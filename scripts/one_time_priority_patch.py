from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def replace_once(path: str, old: str, new: str, count: int = 1):
    p = ROOT / path
    text = p.read_text()
    found = text.count(old)
    if found != count:
        raise SystemExit(f"{path}: expected {count} occurrence(s), found {found}")
    p.write_text(text.replace(old, new, count))

vm_path = "app/src/main/java/com/example/ui/NeuroPathViewModel.kt"
replace_once(
    vm_path,
    """                studentSubject = _selectedSubject.value,\n                schoolDistrict = profile.schoolDistrict,\n                country = profile.country\n""",
    """                studentSubject = _selectedSubject.value,\n                schoolDistrict = profile.schoolDistrict,\n                stateOrProvince = profile.stateOrProvince,\n                country = profile.country\n""",
    2,
)
replace_once(
    vm_path,
    "Learner profile accommodation considerations: ${profile.neurodivergentTypesCsv}.",
    "Learner profile accommodation considerations: use the privacy-safe educational/accessibility signals in the personalization profile; diagnosis labels remain on-device.",
    1,
)
replace_once(
    vm_path,
    """    fun initiateOfflineCurriculumSync() {\n        if (_isDownloadingCurriculum.value) return\n        _isDownloadingCurriculum.value = true\n        viewModelScope.launch {\n            delay(15000)\n            _isDownloadingCurriculum.value = false\n        }\n    }""",
    """    fun initiateOfflineCurriculumSync() {\n        if (_isDownloadingCurriculum.value) return\n        _isDownloadingCurriculum.value = true\n        viewModelScope.launch {\n            try {\n                // Seed/refresh the bundled OER catalog first so the app remains useful offline.\n                repository.initializeOerCurriculum()\n                val profile = _currentProfile.value\n                val resolution = com.example.data.curriculum.CurriculumResolver.resolve(\n                    country = profile.country,\n                    stateOrProvince = profile.stateOrProvince,\n                    schoolDistrict = profile.schoolDistrict,\n                    stageOrGrade = profile.gradeLevel,\n                    subject = _selectedSubject.value.title\n                )\n                val now = System.currentTimeMillis()\n                val existing = repository.getLatestCurriculum()\n                if (existing == null || existing.country != profile.country || existing.stateOrProvince != profile.stateOrProvince) {\n                    repository.saveDownloadedCurriculum(\n                        com.example.data.local.entity.DownloadedCurriculumEntity(\n                            id = "offline_${profile.id}_${resolution.jurisdictionId ?: \"routing\"}",\n                            country = profile.country,\n                            stateOrProvince = profile.stateOrProvince,\n                            schoolDistrict = profile.schoolDistrict,\n                            postalCode = profile.zipOrPostalCodeOverride,\n                            standardTitle = "${resolution.stageOrGrade} / ${resolution.subject}",\n                            officialSourceAgency = resolution.educationAuthority,\n                            officialSourceUrl = resolution.officialSourceUrl,\n                            gradesCoveredSummary = profile.gradeLevel,\n                            rawCurriculumJson = resolution.contextText(),\n                            lastSyncTimestamp = now,\n                            lastSyncDateFormatted = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(now)),\n                            syncStatus = "LOCAL_ROUTING_CACHE"\n                        )\n                    )\n                }\n            } catch (e: Exception) {\n                Log.w("NeuroPathViewModel", "Offline curriculum seed failed; existing cache remains available", e)\n            } finally {\n                _isDownloadingCurriculum.value = false\n            }\n        }\n    }""",
    1,
)
replace_once(
    vm_path,
    "com.example.learning.LearnerPersonalizationEngine.recordAnswer(getApplication(), _currentProfile.value.id, _selectedSubject.value.name, isCorrect, question.questionText.take(100))",
    """com.example.learning.LearnerPersonalizationEngine.recordAnswer(getApplication(), _currentProfile.value.id, _selectedSubject.value.name, isCorrect, question.questionText.take(100))\n        com.example.learning.MasteryRepository.record(getApplication(), _currentProfile.value.id, _selectedSubject.value.name, question.questionText.take(100), isCorrect)""",
    1,
)

# Remove the temporary workflow hook and this script after the patch is committed.
workflow = ROOT / ".github/workflows/build.yml"
w = workflow.read_text()
hook = """\npermissions:\n  contents: write\n"""
w = w.replace(hook, "", 1)
step = """\n      - name: Apply one-time priority patch\n        if: contains(github.event.head_commit.message, '[priority-patch]')\n        run: |\n          python3 scripts/one_time_priority_patch.py\n          git config user.name \"github-actions[bot]\"\n          git config user.email \"41898282+github-actions[bot]@users.noreply.github.com\"\n          git add app scripts .github/workflows/build.yml\n          git diff --cached --check\n          git commit -m \"feat: finish curriculum privacy offline and mastery priorities\"\n          git push origin main\n"""
if step not in w:
    raise SystemExit("temporary workflow hook not found")
w = w.replace(step, "\n", 1)
workflow.write_text(w)

(ROOT / "scripts/one_time_priority_patch.py").unlink()
