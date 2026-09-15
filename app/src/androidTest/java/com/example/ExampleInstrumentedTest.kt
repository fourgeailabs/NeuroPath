package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.repository.NeuroPathRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  @Test
  fun useAppContext() {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals("com.fourgeailabs.neuropath", appContext.packageName)
  }

  @Test
  fun databaseCanInsertAndRetrieveProfile() = runBlocking {
    val db = AppDatabase(appContext)
    val repo = NeuroPathRepository(db)
    
    val profile = ChildProfileEntity(
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
    
    val id = repo.insertProfile(profile)
    assertTrue(id > 0)
    
    val retrieved = repo.getProfileDirect(id)
    assertNotNull(retrieved)
    assertEquals("Test Child", retrieved?.name)
    assertEquals(7, retrieved?.age)
    
    repo.deleteProfile(id)
  }

  @Test
  fun databaseHandlesProfileUpdate() = runBlocking {
    val db = AppDatabase(appContext)
    val repo = NeuroPathRepository(db)
    
    val profile = ChildProfileEntity(
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
    
    val id = repo.insertProfile(profile)
    val retrieved = repo.getProfileDirect(id)!!
    
    val updated = retrieved.copy(name = "Updated Name", age = 8)
    repo.updateProfile(updated)
    
    val final = repo.getProfileDirect(id)!!
    assertEquals("Updated Name", final.name)
    assertEquals(8, final.age)
    
    repo.deleteProfile(id)
  }
}