package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testUserRolePermissions() {
    val owner = com.example.model.UserRole.OWNER
    val user = com.example.model.UserRole.USER
    val mod = com.example.model.UserRole.MODERATOR

    assertTrue(owner.canAccessAdminPanel())
    assertTrue(owner.canBanUsers())
    assertFalse(user.canAccessAdminPanel())
    assertTrue(mod.canAccessAdminPanel())
    assertTrue(mod.canBanUsers())
  }
}
