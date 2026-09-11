package root.shop.accounting.shared

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun messageIsNotBlank() {
        assertTrue(Greeting.message().isNotBlank())
    }
}
