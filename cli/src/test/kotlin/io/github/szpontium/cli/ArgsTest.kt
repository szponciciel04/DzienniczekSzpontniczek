package io.github.szpontium.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArgsTest {
    @Test
    fun parsesOptionsAnywhereAndRepeatedValues() {
        val args = CliArgs(listOf("--json", "login", "jwt", "--token", "one", "--token=two", "--tenant", "school"))
        assertEquals(listOf("login", "jwt"), args.words)
        assertTrue(args.flag("json"))
        assertEquals(listOf("one", "two"), args.values("token"))
        assertEquals("school", args.value("tenant"))
    }

    @Test
    fun supportsEndOfOptions() {
        val args = CliArgs(listOf("profile", "use", "--", "--odd-name"))
        assertEquals(listOf("profile", "use", "--odd-name"), args.words)
        assertFalse(args.flag("odd-name"))
    }
}
