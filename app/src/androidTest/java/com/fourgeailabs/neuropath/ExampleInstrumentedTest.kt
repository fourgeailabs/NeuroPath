package com.fourgeailabs.neuropath

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fourgeailabs.neuropath.data.local.AppDatabase
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.repository.NeuroPathRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

  private fun appContext() = InstrumentationRegistry.getInstrumentation().targetContext

  private fun inMemoryDb(): AppDatabase =
    Room.inMemoryDatabaseBuilder(appContext(), AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

  private fun testProfile() = ChildProfileEntity(
    name = "Test Child",
    age = 7,
    gradeLevel = "GRADE_1",
    ageGroupTier = "ELEMENTARY",
    stateStandard = "CA",
    country = "United States",
    stateOrProvince = "California",
    city = "Los Angeles",
    schoolDistrict = "LAUSD",
    appLanguageCode = "en-US",
    activeThemeId = "dino",
    neurodivergentTypesCsv = "ADHD",
    strugglesCsv = "Focus",
    strengthsCsv = "Visual",
    hyperFixationsCsv = "Dinosaurs"
  )

  @Test
  fun useAppContext() {
    assertEquals("com.fourgeailabs.neuropath", appContext().packageName)
  }

  @Test
  fun databaseCanInsertAndRetrieveProfile() = runBlocking {
    val db = inMemoryDb()
    try {
      val repo = NeuroPathRepository(db)

      val id = repo.insertProfile(testProfile())
      assertTrue(id > 0)

      val retrieved = repo.getProfileDirect(id)
      assertNotNull(retrieved)
      assertEquals("Test Child", retrieved?.name)
      assertEquals(7, retrieved?.age)

      repo.deleteProfile(id)
      assertNull(repo.getProfileDirect(id))
    } finally {
      db.close()
    }
  }

  @Test
  fun databaseHandlesProfileUpdate() = runBlocking {
    val db = inMemoryDb()
    try {
      val repo = NeuroPathRepository(db)

      val id = repo.insertProfile(testProfile())
      val retrieved = repo.getProfileDirect(id)!!

      val updated = retrieved.copy(name = "Updated Name", age = 8)
      repo.updateProfile(updated)

      val final = repo.getProfileDirect(id)!!
      assertEquals("Updated Name", final.name)
      assertEquals(8, final.age)

      repo.deleteProfile(id)
    } finally {
      db.close()
    }
  }
}
