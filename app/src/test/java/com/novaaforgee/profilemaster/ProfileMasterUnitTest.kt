package com.novaaforgee.profilemaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileMasterUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun package_isValid() {
        val pkg = "com.novaaforgee.profilemaster"
        assertTrue(pkg.startsWith("com.novaaforgee"))
    }
}
