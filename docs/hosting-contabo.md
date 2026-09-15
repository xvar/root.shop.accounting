# Хостинг демо на Contabo Cloud VPS

Статус: инстанс создан и доступ подтверждён (2026-09-15).

## 1. Инстанс

- План: **Cloud VPS 4** — 4 vCPU, 8 GB RAM, 100 GB SSD.
- Контракт: 24 месяца, €5.24/мес (включая имеющиеся add-ons — цена в
  панели может отличаться от прайс-листа на сайте на пару центов).
- ОС: Ubuntu 26.04.1 LTS.
- Публичный IPv4: `13.140.152.178`.

## 2. SSH-доступ

⚠️ **Важный нюанс**: панель Contabo в карточке сервера показывает
"Default User: **admin**" — это НЕ системное имя пользователя, это просто
лейбл в UI. Реальный логин на Ubuntu cloud-образе — стандартный **`ubuntu`**
(сервер сам подсказывает это, если по ошибке зайти под `root`: *"Please
login as the user 'ubuntu' rather than the user 'root'"*).

```bash
ssh -i ~/.ssh/smilingzen_vps ubuntu@13.140.152.178
```

SSH-ключ добавлен через панель Contabo при создании инстанса (шаг
установки/переустановки образа → добавление публичного ключа), пароль не
использовался.

Приложение не устанавливалось при создании (ни cloud-init, ни 1-Click App)
— чистая ОС, весь сетап (Docker и т.д.) делаем вручную по SSH, как в
`hosting-oracle-free-tier.md` §5.

## 3. Firewall

У новых VPS/VDS Contabo фаервол включён по умолчанию — **весь входящий
трафик заблокирован**, исходящий не ограничен (официально:
[Firewall: What is it and how does it protect my VPS/VDS?](https://help.contabo.com/en/support/solutions/articles/103000390430-firewall-what-is-it-and-how-does-it-protect-my-vps-vds-)).

Добавленные правила (Servers & Hosting → VPS → сервер → вкладка Firewall):

| Type of traffic | Protocol | Port | Source |
|---|---|---|---|
| SSH | TCP | 22 | Specific IP address (свой публичный IP) либо Any IPv4, если IP динамический |
| HTTP | TCP | 80 | Any |
| HTTPS | TCP | 443 | Any |

Порт 80 нужен даже без прямого использования — через него Let's Encrypt
проходит HTTP-01 challenge при выпуске TLS-сертификата для reverse proxy.
Порты БД (Postgres и т.д.) наружу не открываются — только внутри
docker-сети.

## 4. Что дальше (не сделано ещё)

Остальные вкладки панели (Snapshots, Auto Backup, Images, Private Network,
Additional IPs, Licenses, DNS Management) на этом шаге сознательно
пропущены — не блокируют старт:

- **Auto Backup** — сейчас "Add-On required" (платно, выключено).
  Решить позже: платный аддон Contabo либо свой скрипт бэкапа
  docker-volume с БД.
- **DNS Management** — не используем, домен `smilingzen.dev` ведётся на
  Porkbun (см. §6 ниже), Contabo DNS не нужен.
- Деплой самого backend'а store-service (Docker-образ → k3s Deployment) —
  ещё не сделан, будет частью реализации фич (см. design.md §6/§9).

## 5. Оркестрация — k3s (вместо голого Docker Compose + Caddy)

Решение принято 2026-09-15: вместо связки docker-compose + Caddy сразу
ставим **k3s** (лёгкий Kubernetes, дистрибутив Rancher) — пользователь
явно попросил сразу масштабируемое решение, раз на сервере будет
несколько проектов/поддоменов, а не одно демо. Обоснование выбора
k3s vs Caddy было заранее описано в `hosting-oracle-free-tier.md` §7 —
там это фигурировало как "шаг для будущего", теперь делаем его сразу.

Установлено и проверено (2026-09-15):

```bash
curl -sfL https://get.k3s.io | sudo sh -
```

- Версия: **k3s v1.36.4+k3s1**, канал stable.
- Единственная нода (control-plane), статус `Ready`.
- Встроенный ингресс-контроллер **Traefik** поднялся автоматически
  (ставится через helm-install хуки k3s из коробки, отдельно ничего не
  накатывали) и слушает на публичном IP через встроенный ServiceLB:
  `traefik LoadBalancer 13.140.152.178 80:xxxxx/TCP,443:xxxxx/TCP` — то
  есть 80/443 уже проксируются Traefik'ом, отдельный Caddy не нужен.
- `kubectl` — симлинк на бинарник `k3s`, на сервере команды выполнять как
  `sudo kubectl ...` (или добавить `~/.kube/config` с правильными правами
  для запуска без sudo — пока не делали).
- Локальный kubeconfig на рабочую машину **не выгружали** — сейчас
  управление кластером идёт через SSH + `sudo kubectl` на самом сервере;
  если понадобится дёргать кластер с ноутбука/CI — вынести
  `/etc/rancher/k3s/k3s.yaml`, подменив `server:` на публичный IP.

TODO (не сделано, будет по ходу реализации backend-фичи):

- [ ] TLS для Ingress — на выбор: **cert-manager** + `ClusterIssuer`
  (Let's Encrypt, отдельный контроллер) либо встроенный в Traefik
  ACME-провайдер (проще, меньше сущностей, но менее гибко при большом
  числе доменов) — решить перед первым Ingress.
- [ ] Container registry для образов store-service — Docker Hub (простой
  публичный, но приватность/лимиты pull-rate) vs GitHub Container Registry
  (ghcr.io, привязан к тому же GitHub-аккаунту/репо) — не выбрано.
- [ ] Namespace-стратегия — один namespace на проект (`store-service`,
  будущие демо в своих) или всё в `default` — решить перед первым деплоем.

## 6. Домен

Пользователь уже владеет `smilingzen.dev` (Porkbun) и использует его как
общий домен для всех демо-проектов/песочниц, по схеме
`api.<project>.smilingzen.dev` на один поддомен на проект. Для
store-service: **`api.stores.smilingzen.dev`**. DNS-записи пользователь
заводит вручную сам (не через Contabo DNS Management, не через API).

- [ ] В Porkbun добавить DNS-запись (сделает пользователь, когда дойдём
  до реального деплоя backend'а — заранее заводить незачем, IP сервера
  уже не изменится):

  | Type | Host | Answer | TTL |
  |---|---|---|---|
  | A | `api.stores` (при домене `smilingzen.dev`) | `13.140.152.178` | 300 (можно поднять после проверки) |

- [ ] Когда DNS-запись будет добавлена — создать `Ingress`-ресурс для
  store-service с host `api.stores.smilingzen.dev`, маршрутизирующий на
  `Service` бэкенда; Traefik подхватит его автоматически (Ingress Class
  по умолчанию — `traefik`, ничего дополнительно указывать не нужно).
- [ ] Следующее демо — та же схема: новая A-запись
  `api.<project>.smilingzen.dev` + новый `Ingress` в том же кластере,
  Caddyfile редактировать не придётся (это и была цель перехода на k3s).
