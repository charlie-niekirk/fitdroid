package com.fitdroid.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleHealthScopesTest {
    @Test
    fun all_containsRestrictedReadonlyScopesOnly() {
        assertEquals(3, GoogleHealthScopes.all.size)
        assertTrue(GoogleHealthScopes.all.all { it.startsWith("https://www.googleapis.com/auth/googlehealth.") })
        assertTrue(GoogleHealthScopes.all.all { it.endsWith(".readonly") })
    }
}
