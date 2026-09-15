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
- **DNS Management** — понадобится только если вести DNS домена через
  Contabo; домен для демо ещё не выбран (см. design.md §7).
- Базовая настройка сервера (обновления, Docker, Caddy) — по аналогии с
  `hosting-oracle-free-tier.md` §5–6, ещё предстоит сделать на этом
  инстансе.
