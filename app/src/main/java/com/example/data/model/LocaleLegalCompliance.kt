package com.example.data.model

data class LocaleLegalNotice(
    val country: String,
    val countryCode: String,
    val flagEmoji: String,
    val privacyLawTitle: String,
    val privacyLawStatute: String,
    val educationalAuthorityTitle: String,
    val educationalStandardsName: String,
    val coppaOrEquivalentDescription: String,
    val ageOfConsent: String,
    val termsSections: List<LocaleTermsSection>,
    val regionalEducationalGuidelines: List<String>,
    val restrictedForeignCurricula: List<String>
) {
    val countryName: String get() = country
    val governingLaw: String get() = "$privacyLawTitle ($privacyLawStatute)"
    val aiMistakesWarning: String get() = termsSections.find { it.title.contains("Artificial Intelligence", ignoreCase = true) || it.title.contains("AI", ignoreCase = true) }?.content
        ?: "Artificial Intelligence models can make mistakes, hallucinate facts, or provide unexpected responses just like any other AI tool in the world. NeuroPath enforces strict child-safe boundaries, but parent or guardian supervision and periodic review of learning sessions are always recommended."
    val internetAccessNotice: String get() = termsSections.find { it.title.contains("Internet", ignoreCase = true) }?.content
        ?: "Internet access is used at defined points during app execution solely to connect to Cloud Artificial Intelligence services (Google Gemini) for real-time speech processing, tutoring hints, and curriculum updates."
    val locationCurriculumNotice: String get() = termsSections.find { it.title.contains("Location", ignoreCase = true) }?.content
        ?: "Location data is accessed solely on-device to verify home country educational compliance and prevent access to foreign curricula or standards."
}

data class LocaleTermsSection(
    val title: String,
    val content: String
) {
    val sectionTitle: String get() = title
}

object LocaleLegalComplianceManager {

    fun getComplianceNotice(country: String): LocaleLegalNotice {
        return when {
            country.contains("United States", ignoreCase = true) || country.equals("US", ignoreCase = true) -> getUSNotice()
            country.contains("United Kingdom", ignoreCase = true) || country.equals("GB", ignoreCase = true) || country.equals("UK", ignoreCase = true) -> getUKNotice()
            country.contains("Canada", ignoreCase = true) || country.equals("CA", ignoreCase = true) -> getCanadaNotice()
            country.contains("Australia", ignoreCase = true) || country.equals("AU", ignoreCase = true) -> getAustraliaNotice()
            country.contains("India", ignoreCase = true) || country.equals("IN", ignoreCase = true) -> getIndiaNotice()
            country.contains("Germany", ignoreCase = true) || country.equals("DE", ignoreCase = true) -> getGermanyNotice()
            country.contains("France", ignoreCase = true) || country.equals("FR", ignoreCase = true) -> getFranceNotice()
            country.contains("Japan", ignoreCase = true) || country.equals("JP", ignoreCase = true) -> getJapanNotice()
            country.contains("Brazil", ignoreCase = true) || country.equals("BR", ignoreCase = true) -> getBrazilNotice()
            country.contains("Mexico", ignoreCase = true) || country.equals("MX", ignoreCase = true) -> getMexicoNotice()
            else -> getGlobalDefaultNotice(country)
        }
    }

    private fun getUSNotice() = LocaleLegalNotice(
        country = "United States",
        countryCode = "US",
        flagEmoji = "🇺🇸",
        privacyLawTitle = "COPPA, FERPA & CIPA Compliance",
        privacyLawStatute = "15 U.S.C. §§ 6501–6506 (COPPA) / 34 CFR Part 99 (FERPA) / 47 U.S.C. § 254(h) (CIPA)",
        educationalAuthorityTitle = "US Dept of Education & State Boards of Education",
        educationalStandardsName = "Common Core State Standards (CCSS), NGSS, TEKS & State Frameworks",
        coppaOrEquivalentDescription = "Complies strictly with the Children's Online Privacy Protection Act (COPPA) & Family Educational Rights and Privacy Act (FERPA). No student personal data or audio is commercialized or sold.",
        ageOfConsent = "Verifiable Parental Consent required for children under 13",
        regionalEducationalGuidelines = listOf(
            "Common Core State Standards (CCSS) for Mathematics and English Language Arts",
            "Next Generation Science Standards (NGSS) K-12 Inquiry Science Framework",
            "State-specific benchmarks (e.g. Texas TEKS, Florida B.E.S.T., NY Next Gen, California Frameworks)",
            "Standard US Grade Level Structure (Pre-K to 12th Grade)",
            "US Customary & Metric units of measurement",
            "US Dollar ($ / USD) financial literacy concepts"
        ),
        restrictedForeignCurricula = listOf(
            "UK Key Stages & GCSE National Curriculum frameworks",
            "Australian ACARA / Victorian / NSW syllabus numbering",
            "Indian CBSE / NCERT board examination structures",
            "Canadian Provincial Ministères de l'Éducation frameworks",
            "Non-US localized spelling and currency systems"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. United States Educational Jurisdiction & Standards Compliance",
                content = "NeuroPath is configured to operate in full alignment with accredited United States educational standards, including the Common Core State Standards (CCSS), Next Generation Science Standards (NGSS), and individual State Department of Education benchmarks (such as California CA-CCSS, Texas TEKS, Florida B.E.S.T., and New York State P-12 Standards). Foreign educational frameworks and non-US curricular structures are strictly restricted to prevent academic divergence."
            ),
            LocaleTermsSection(
                title = "2. Children's Online Privacy Protection Act (COPPA & FERPA)",
                content = "Under 15 U.S.C. §§ 6501–6506 and 34 CFR Part 99, NeuroPath does not sell, lease, or monetize student personal information, nor does it create behavioral advertising profiles. Voice dictation audio is processed in real time via secure cloud APIs and is never retained for biometric profiling. All learner metrics, rewards, and sensory progress logs remain stored locally on your device."
            ),
            LocaleTermsSection(
                title = "3. Location Data Utilization & Geographic Curriculum Locking",
                content = "Location services are accessed exclusively on-device for the sole purpose of verifying that the student's learning modules conform to their accredited United States school district and state guidelines. Location coordinates are never transmitted to external marketing servers, stored on remote databases, or shared with third parties."
            ),
            LocaleTermsSection(
                title = "4. Internet Access & Cloud AI Connectivity Notice",
                content = "Internet access is used at defined points during app execution solely to connect to Cloud Artificial Intelligence services (Google Gemini) for real-time speech-to-text transcription, adaptive neurodivergent scaffolding, and synchronizing accredited curriculum updates."
            ),
            LocaleTermsSection(
                title = "5. Artificial Intelligence Accuracy & Limitations Disclaimer",
                content = "NeuroPath utilizes Generative Artificial Intelligence to support differentiated learning. Like all AI systems in the world, AI models may occasionally generate inaccurate facts, hallucinations, or unexpected outputs. Active parental supervision and verification of lesson progress is strongly advised."
            ),
            LocaleTermsSection(
                title = "6. Non-Clinical Educational & Sensory Support",
                content = "NeuroPath is an interactive digital learning companion and sensory accommodation tool. It is not a medical device and does not provide clinical diagnostic therapy, psychological treatment, or replace formal school Individualized Education Programs (IEP) / 504 plans."
            )
        )
    )

    private fun getUKNotice() = LocaleLegalNotice(
        country = "United Kingdom",
        countryCode = "GB",
        flagEmoji = "🇬🇧",
        privacyLawTitle = "UK GDPR, DPA 2018 & Age Appropriate Design Code",
        privacyLawStatute = "Data Protection Act 2018 / UK GDPR (ICO Children's Code Standards)",
        educationalAuthorityTitle = "Department for Education (DfE) & Education Scotland",
        educationalStandardsName = "UK National Curriculum (Key Stages 1-4) & Scottish Curriculum for Excellence (CfE)",
        coppaOrEquivalentDescription = "Complies strictly with the Information Commissioner's Office (ICO) 15 Standards of Age Appropriate Design. Default high privacy settings, no profiling, and child's best interests prioritized.",
        ageOfConsent = "Parental consent required for children under 13 under UK GDPR",
        regionalEducationalGuidelines = listOf(
            "UK National Curriculum Programmes of Study (Key Stage 1, Key Stage 2, Key Stage 3)",
            "Scottish Curriculum for Excellence (CfE) Levels (Early, First, Second)",
            "British English phonics (Letters and Sounds framework) and vocabulary",
            "Pounds and Pence (£ / GBP) currency and Metric measurement system",
            "DfE Social, Emotional and Mental Health (SEMH) educational recommendations"
        ),
        restrictedForeignCurricula = listOf(
            "US Common Core (CCSS) & US State Standards (TEKS, B.E.S.T.)",
            "Australian ACARA Curriculum",
            "Indian CBSE / NCERT Syllabus",
            "US Customary imperial units and foreign currency concepts"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. United Kingdom Educational Framework Compliance",
                content = "NeuroPath is tailored strictly for use under the UK National Curriculum (covering Early Years Foundation Stage and Key Stages 1 to 4) and Education Scotland's Curriculum for Excellence (CfE). Curricular content, spellings, metric units, and currency references (£ GBP) conform to Department for Education (DfE) standards. Foreign educational frameworks are restricted."
            ),
            LocaleTermsSection(
                title = "2. ICO Age Appropriate Design Code (Children's Code) Compliance",
                content = "In full compliance with the 15 standards of the ICO Children's Code, NeuroPath operates with high privacy by default. The best interests of the child are our primary consideration. Geolocation is turned off by default except for localized educational standard confirmation, and child profiling is strictly prohibited."
            ),
            LocaleTermsSection(
                title = "3. Geographic Location Usage for Educational Verification",
                content = "Location data is processed strictly on-device to confirm the learner's UK education authority jurisdiction and prevent access to foreign curricular materials. Location data is never retained, logged, or shared."
            ),
            LocaleTermsSection(
                title = "4. Internet Access & Cloud AI Processing",
                content = "Internet access is strictly utilized to communicate with Cloud AI infrastructure for real-time speech evaluation and accredited DfE curriculum updates. Audio data is processed ephemerally without persistent identification."
            ),
            LocaleTermsSection(
                title = "5. AI Accuracy & Parental Responsibility Notice",
                content = "Generative AI models are subject to occasional hallucination or inaccuracy. In accordance with UK online safety principles, parent or guardian guidance is recommended during interactive learning sessions."
            )
        )
    )

    private fun getCanadaNotice() = LocaleLegalNotice(
        country = "Canada",
        countryCode = "CA",
        flagEmoji = "🇨🇦",
        privacyLawTitle = "PIPEDA & Provincial Privacy & Child Protection Acts",
        privacyLawStatute = "Personal Information Protection and Electronic Documents Act (PIPEDA) / Quebec Law 25",
        educationalAuthorityTitle = "Provincial Ministries of Education (Ontario, Quebec, BC, Alberta)",
        educationalStandardsName = "Ontario Curriculum, Quebec PFEQ, BC Building Student Success & Alberta Programs of Study",
        coppaOrEquivalentDescription = "Complies with the Office of the Privacy Commissioner of Canada (OPC) guidelines on youth privacy. Explicit parental consent and strict data limitation.",
        ageOfConsent = "Parental consent required for children under 14 (under provincial standards)",
        regionalEducationalGuidelines = listOf(
            "Ontario Ministry of Education Elementary & Secondary Curriculum",
            "Programme de Formation de l'École Québécoise (PFEQ)",
            "British Columbia Concept-Based Competency-Driven Curriculum",
            "Canadian English and Canadian French bilingual alignment",
            "Canadian Dollars ($ CAD) and Metric SI measurement standards"
        ),
        restrictedForeignCurricula = listOf(
            "US State-specific curricula (TEKS, FL B.E.S.T.)",
            "UK Key Stages systems",
            "Australian / Indian board examination structures"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. Canadian Provincial Educational Standards Compliance",
                content = "NeuroPath aligns its subject lessons with Canadian Provincial Educational guidelines, including the Ontario Ministry of Education Curriculum, Quebec PFEQ, and BC Student Success framework. Content adheres to Canadian standards and bilingual considerations."
            ),
            LocaleTermsSection(
                title = "2. PIPEDA & Youth Privacy Protection",
                content = "Under PIPEDA and relevant provincial statutes (e.g. Ontario FIPPA/MFIPPA, Quebec Law 25), personal information belonging to minors is granted heightened protection. No student data is commercialized or transferred outside secure transient processing."
            ),
            LocaleTermsSection(
                title = "3. Geographic Location Verification",
                content = "Location data is strictly used on-device to verify Canadian provincial jurisdiction and lock the curriculum to accredited Canadian standards. Location data is never tracked or shared."
            ),
            LocaleTermsSection(
                title = "4. Cloud Connectivity & AI Tools Disclaimer",
                content = "Internet connectivity is employed to access cloud AI engines for natural speech dictation and curriculum downloads. Parents are reminded that generative AI models may occasionally make factual errors and require parental guidance."
            )
        )
    )

    private fun getAustraliaNotice() = LocaleLegalNotice(
        country = "Australia",
        countryCode = "AU",
        flagEmoji = "🇦🇺",
        privacyLawTitle = "Privacy Act 1988 (Cth) & Online Safety Act 2021",
        privacyLawStatute = "Privacy Act 1988 (Cth) - Australian Privacy Principles (APPs) / OAIC Child Guidelines",
        educationalAuthorityTitle = "ACARA (Australian Curriculum, Assessment and Reporting Authority)",
        educationalStandardsName = "Australian Curriculum (F-10), NSW NESA Syllabuses & Victorian Curriculum",
        coppaOrEquivalentDescription = "Complies with the OAIC's strict standards for handling children's personal information and eSafety Commissioner youth guidelines.",
        ageOfConsent = "Parental consent required for individuals under 15",
        regionalEducationalGuidelines = listOf(
            "ACARA Australian Curriculum Foundation to Year 10 (F-10)",
            "NSW Education Standards Authority (NESA) Syllabuses",
            "Victorian Curriculum and Assessment Authority (VCAA) Framework",
            "Australian English spelling and Australian Dollars ($ AUD)",
            "Metric measurement standards"
        ),
        restrictedForeignCurricula = listOf(
            "US Common Core and State Frameworks",
            "UK Key Stage examinations",
            "Non-Australian educational systems"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. Australian National & State Curriculum Alignment",
                content = "NeuroPath lessons are strictly mapped to the Australian Curriculum (ACARA Version 9.0), NSW NESA syllabuses, and Victorian VCAA frameworks. Out-of-region educational standards are locked out to ensure continuity with Australian schooling."
            ),
            LocaleTermsSection(
                title = "2. Australian Privacy Principles (APPs) & Child Protection",
                content = "In accordance with the Privacy Act 1988 (Cth) and OAIC guidelines, student data is kept strictly private. Transient voice data for speech assistance is never retained for commercial exploitation."
            ),
            LocaleTermsSection(
                title = "3. Location Verification Policy",
                content = "Device location is used exclusively on-device to confirm the child's Australian state jurisdiction and verify curriculum compliance. Location coordinates are never uploaded to servers."
            ),
            LocaleTermsSection(
                title = "4. Internet Access & Educational AI Transparency",
                content = "Internet connection is required to interact with cloud AI features and download syllabus updates. Parents should note that AI models can make errors and must be accompanied by responsible guardian oversight."
            )
        )
    )

    private fun getIndiaNotice() = LocaleLegalNotice(
        country = "India",
        countryCode = "IN",
        flagEmoji = "🇮🇳",
        privacyLawTitle = "Digital Personal Data Protection Act, 2023 (DPDP Act)",
        privacyLawStatute = "DPDP Act, 2023 - Section 9 (Processing of Personal Data of Children)",
        educationalAuthorityTitle = "National Council of Educational Research and Training (NCERT) & CBSE",
        educationalStandardsName = "National Education Policy (NEP 2020) & NCERT / CBSE National Curriculum Framework",
        coppaOrEquivalentDescription = "In strict accordance with Section 9 of the DPDP Act 2023: Verifiable parental consent is required. Tracking, behavioral monitoring of children, or targeted advertising is strictly prohibited.",
        ageOfConsent = "Verifiable parental consent required for all children under 18",
        regionalEducationalGuidelines = listOf(
            "National Education Policy (NEP 2020) Competency-Based Learning",
            "NCERT National Curriculum Framework for Foundational and Preparatory Stages",
            "CBSE / ICSE Core Academic Learning Outlines",
            "Indian Rupee (₹ / INR) currency and metric system",
            "Indian multi-lingual and cultural context"
        ),
        restrictedForeignCurricula = listOf(
            "US Common Core and State standards",
            "UK Key Stages",
            "Australian ACARA frameworks"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. Indian National Curriculum (NEP 2020 & NCERT) Alignment",
                content = "NeuroPath complies strictly with the National Education Policy 2020 (NEP 2020) and the NCERT National Curriculum Framework. Lessons emphasize conceptual clarity, foundational numeracy, and multilingual support. Foreign educational syllabi are restricted."
            ),
            LocaleTermsSection(
                title = "2. DPDP Act 2023 (Section 9) Child Privacy Safeguards",
                content = "In compliance with Section 9 of India's Digital Personal Data Protection Act 2023: (i) Verifiable parental consent is mandatory before app use; (ii) NeuroPath undertakes NO behavioral monitoring, profiling, or tracking of children; (iii) No targeted advertising is served; (iv) Child data is processed with extreme care to prevent any adverse effect on child well-being."
            ),
            LocaleTermsSection(
                title = "3. On-Device Location Usage for Regional Syllabus",
                content = "Location is accessed solely on-device to verify Indian state/board jurisdiction and prevent access to non-aligned international curricula. No location telemetry is stored remotely."
            ),
            LocaleTermsSection(
                title = "4. AI Accuracy & Internet Connectivity",
                content = "Internet connectivity powers cloud AI speech evaluation and NCERT curriculum updates. Parents are advised that AI models may make occasional factual mistakes."
            )
        )
    )

    private fun getGermanyNotice() = LocaleLegalNotice(
        country = "Germany",
        countryCode = "DE",
        flagEmoji = "🇩🇪",
        privacyLawTitle = "EU DSGVO / BDSG & EU AI Act Compliance",
        privacyLawStatute = "Datenschutz-Grundverordnung (DSGVO Art. 8) / Bundesdatenschutzgesetz (BDSG)",
        educationalAuthorityTitle = "Kultusministerkonferenz (KMK) & Landeslehrpläne",
        educationalStandardsName = "Bayerischer LehrplanPLUS, Berliner Rahmenlehrplan & KMK Bildungsstandards",
        coppaOrEquivalentDescription = "Strenge Einhaltung von Art. 8 DSGVO. Keine Weitergabe von Schülerdaten, standardmäßig aktivierte Datensparsamkeit und lokale Speicherung.",
        ageOfConsent = "Einwilligung der Eltern für Kinder unter 16 Jahren erforderlich",
        regionalEducationalGuidelines = listOf(
            "KMK Bildungsstandards für Primarstufe und Sekundarstufe I",
            "Landesspezifische Lehrpläne (z.B. LehrplanPLUS Bayern)",
            "Euro (€ / EUR) Währung und metrisches System",
            "Deutsche Rechtschreibung und Grammatik"
        ),
        restrictedForeignCurricula = listOf(
            "US Common Core",
            "UK Key Stages",
            "Nicht-europäische Bildungsprogramme"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. Einhaltung der deutschen Bildungsstandards (KMK)",
                content = "NeuroPath orientiert sich an den Bildungsstandards der Kultusministerkonferenz (KMK) und den Rahmenlehrplänen der Bundesländer. Fremde internationale Lehrpläne sind gesperrt."
            ),
            LocaleTermsSection(
                title = "2. Datenschutz gemäß DSGVO (Art. 8) & BDSG",
                content = "Gemäß Art. 8 DSGVO werden keine personenbezogenen Daten von Minderjährigen für kommerzielle Zwecke oder Profiling verarbeitet. Die Sprachverarbeitung erfolgt flüchtig über verschlüsselte Schnittstellen."
            ),
            LocaleTermsSection(
                title = "3. Standortdaten und Geoblocking fremder Lehrinhalte",
                content = "Standortdaten werden ausschließlich lokal auf dem Gerät genutzt, um die Gültigkeit des bundeslandspezifischen Lehrplans zu bestätigen."
            ),
            LocaleTermsSection(
                title = "4. Transparenz bezüglich Cloud-KI und Internetzugriff",
                content = "Internetzugriff wird ausschließlich zur Kommunikation mit Cloud-KI-Diensten zur Spracherkennung und für Lehrplan-Updates genutzt. Eltern werden darauf hingewiesen, dass KI Fehler machen kann."
            )
        )
    )

    private fun getFranceNotice() = LocaleLegalNotice(
        country = "France",
        countryCode = "FR",
        flagEmoji = "🇫🇷",
        privacyLawTitle = "RGPD & Loi Informatique et Libertés",
        privacyLawStatute = "Règlement Général sur la Protection des Données (RGPD Art. 8) / Loi CNIL",
        educationalAuthorityTitle = "Ministère de l'Éducation Nationale",
        educationalStandardsName = "Socle Commun de Connaissances, de Compétences et de Culture",
        coppaOrEquivalentDescription = "Conformité stricte avec les recommandations CNIL et le RGPD pour la protection des mineurs. Données stockées localement.",
        ageOfConsent = "Consentement parental requis pour les moins de 15 ans",
        regionalEducationalGuidelines = listOf(
            "Programmes du cycle 2 et 3 de l'école élémentaire",
            "Socle commun de connaissances et de compétences",
            "Devise Euro (€ / EUR) et système métrique",
            "Orthographe et grammaire françaises"
        ),
        restrictedForeignCurricula = listOf(
            "Programmes US Common Core",
            "Curricula non conformes à l'Éducation Nationale"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. Conformité aux Programmes de l'Éducation Nationale",
                content = "NeuroPath est conçu pour respecter le socle commun de connaissances, de compétences et de culture du Ministère de l'Éducation Nationale."
            ),
            LocaleTermsSection(
                title = "2. Protection des données des mineurs (RGPD & CNIL)",
                content = "Aucune donnée personnelle n'est vendue ni utilisée à des fins de profilage publicitaire. Les enregistrements vocaux sont traités en temps réel sans conservation."
            ),
            LocaleTermsSection(
                title = "3. Données de localisation et restriction géographique",
                content = "La localisation sert exclusivement sur l'appareil à confirmer la conformité au cadre éducatif français."
            ),
            LocaleTermsSection(
                title = "4. Avertissement sur l'Intelligence Artificielle",
                content = "L'IA générative peut produire des inexactitudes factuelles. La supervision parentale est recommandée."
            )
        )
    )

    private fun getJapanNotice() = LocaleLegalNotice(
        country = "Japan",
        countryCode = "JP",
        flagEmoji = "🇯🇵",
        privacyLawTitle = "個人情報保護法 (APPI) & 文部科学省ガイドライン",
        privacyLawStatute = "Act on the Protection of Personal Information (APPI) / MEXT AI in Education Guidelines",
        educationalAuthorityTitle = "文部科学省 (MEXT)",
        educationalStandardsName = "学習指導要領 (MEXT Course of Study for Elementary & Junior High)",
        coppaOrEquivalentDescription = "文部科学省の初等中等教育段階における生成AI利用ガイドラインに準拠。子どもの個人情報の適切な管理と保護を実施。",
        ageOfConsent = "15歳未満の利用には保護者の同意が必須",
        regionalEducationalGuidelines = listOf(
            "文部科学省 学習指導要領（小学校・中学校）",
            "日本語表記（ひらがな・カタカナ・常用漢字配当）",
            "日本円（¥ / JPY）およびメートル法単位",
            "日本の道徳・生活科・総合的な学習の理念"
        ),
        restrictedForeignCurricula = listOf(
            "米国のCommon Core基準",
            "英国Key Stage教育体系",
            "海外のカリキュラムツール"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. 文部科学省 学習指導要領への適合",
                content = "NeuroPathは文部科学省の学習指導要領に準拠し、日本の初等教育カリキュラムに沿った学習を提供します。他国のカリキュラム体系へのアクセスは制限されています。"
            ),
            LocaleTermsSection(
                title = "2. 個人情報保護法 (APPI) およびプライバシー保護",
                content = "子どもの個人情報は商用利用や行動追跡に利用されず、端末内で安全に管理されます。音声データはリアルタイム処理のみ行われます。"
            ),
            LocaleTermsSection(
                title = "3. 位置情報の限定利用",
                content = "位置情報は日本国内の教育基準確認および外国カリキュラム遮断のために端末内でのみ利用され、外部送信されません。"
            ),
            LocaleTermsSection(
                title = "4. AIの正確性およびインターネット利用に関する免責事項",
                content = "生成AIは誤った情報を生成する可能性があります。学習の際は保護者の確認と指導を推奨します。"
            )
        )
    )

    private fun getBrazilNotice() = LocaleLegalNotice(
        country = "Brazil",
        countryCode = "BR",
        flagEmoji = "🇧🇷",
        privacyLawTitle = "LGPD (Lei Geral de Proteção de Dados - Art. 14)",
        privacyLawStatute = "Lei Federal nº 13.709/2018 (LGPD - Tratamento de dados de crianças e adolescentes)",
        educationalAuthorityTitle = "Ministério da Educação (MEC)",
        educationalStandardsName = "Base Nacional Comum Curricular (BNCC)",
        coppaOrEquivalentDescription = "Tratamento de dados no melhor interesse da criança com consentimento específico dos pais (Art. 14 da LGPD). Sem rastreamento comportamental.",
        ageOfConsent = "Consentimento dos pais obrigatório para menores de 18 anos",
        regionalEducationalGuidelines = listOf(
            "Base Nacional Comum Curricular (BNCC) para Educação Infantil e Fundamental",
            "Currículo da Cidade e Diretrizes Estaduais",
            "Real Brasileiro (R$ / BRL) e Sistema Métrico",
            "Língua Portuguesa (Brasil)"
        ),
        restrictedForeignCurricula = listOf(
            "US Common Core",
            "Curricula estrangeiros não alinhados à BNCC"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. Alinhamento com a Base Nacional Comum Curricular (BNCC)",
                content = "O NeuroPath segue as diretrizes da BNCC do Ministério da Educação (MEC). Conteúdos fora do padrão curricular brasileiro são restritos."
            ),
            LocaleTermsSection(
                title = "2. Proteção de Dados de Menores (LGPD Art. 14)",
                content = "Em cumprimento à LGPD, nenhum dado de criança é vendido ou utilizado para publicidade direcionada. O processamento de voz ocorre de forma transitória."
            ),
            LocaleTermsSection(
                title = "3. Uso Restrito da Localização",
                content = "A localização é verificada exclusivamente no dispositivo para garantir o enquadramento nas diretrizes educacionais nacionais."
            ),
            LocaleTermsSection(
                title = "4. Uso de Inteligência Artificial e Conectividade",
                content = "A internet é utilizada para conectar à IA na nuvem. Os pais devem estar cientes de que a IA pode cometer erros e a supervisão é incentivada."
            )
        )
    )

    private fun getMexicoNotice() = LocaleLegalNotice(
        country = "Mexico",
        countryCode = "MX",
        flagEmoji = "🇲🇽",
        privacyLawTitle = "LFPDPPP & Ley General de los Derechos de Niñas, Niños y Adolescentes",
        privacyLawStatute = "Ley Federal de Protección de Datos Personales (LFPDPPP) / LGDNNA",
        educationalAuthorityTitle = "Secretaría de Educación Pública (SEP)",
        educationalStandardsName = "Plan de Estudios de la Nueva Escuela Mexicana (SEP)",
        coppaOrEquivalentDescription = "Protección de datos de menores conforme al INAI y LGDNNA. Consentimiento parental expreso y datos locales.",
        ageOfConsent = "Consentimiento parental obligatorio para menores de 18 años",
        regionalEducationalGuidelines = listOf(
            "Programas de Estudio de la Nueva Escuela Mexicana (SEP)",
            "Campos Formativos de Educación Básica",
            "Peso Mexicano ($ / MXN) y Sistema Métrico Decimal",
            "Español de México"
        ),
        restrictedForeignCurricula = listOf(
            "US Common Core",
            "Sistemas educativos no pertenecientes a la SEP"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. Alineación con los Programas de la SEP (Nueva Escuela Mexicana)",
                content = "NeuroPath se encuentra alineado a los programas de la Secretaría de Educación Pública (SEP). Se restringe el uso de herramientas extranjeras no homologadas."
            ),
            LocaleTermsSection(
                title = "2. Privacidad de Menores (LFPDPPP & INAI)",
                content = "Los datos del estudiante están protegidos conforme a la legislación mexicana. No se realiza perfilamiento comercial de los menores."
            ),
            LocaleTermsSection(
                title = "3. Verificación de Ubicación",
                content = "La ubicación se procesa únicamente en el dispositivo para verificar los lineamientos educativos locales."
            ),
            LocaleTermsSection(
                title = "4. Descargo sobre Inteligencia Artificial e Internet",
                content = "La IA puede presentar errores ocasionales. Se recomienda el acompañamiento de padres y tutores."
            )
        )
    )

    private fun getGlobalDefaultNotice(countryName: String) = LocaleLegalNotice(
        country = countryName,
        countryCode = "GLOBAL",
        flagEmoji = "🌐",
        privacyLawTitle = "UN Convention on the Rights of the Child & Child Privacy Standards",
        privacyLawStatute = "UNCRC Articles 16 & 17 / Global Child Online Protection Guidelines",
        educationalAuthorityTitle = "National Ministry / Department of Education",
        educationalStandardsName = "Accredited National Educational Guidelines for $countryName",
        coppaOrEquivalentDescription = "Applies strict global child data minimization, on-device storage, zero advertising tracking, and verified parental consent.",
        ageOfConsent = "Parental consent required for minors according to national law",
        regionalEducationalGuidelines = listOf(
            "National curriculum guidelines for $countryName",
            "Country-specific language and spelling conventions",
            "Local currency and international metric measurement standards"
        ),
        restrictedForeignCurricula = listOf(
            "Foreign curriculum tools and grade systems not applicable in $countryName"
        ),
        termsSections = listOf(
            LocaleTermsSection(
                title = "1. National Educational Standards Compliance",
                content = "NeuroPath is configured to respect the accredited educational guidelines of $countryName. Educational tools or curricula designed strictly for foreign jurisdictions are restricted to avoid academic conflict."
            ),
            LocaleTermsSection(
                title = "2. Child Online Privacy & Data Protection",
                content = "In alignment with the United Nations Convention on the Rights of the Child (UNCRC) and international child privacy standards, NeuroPath never sells student data or performs commercial behavioral profiling. Data is stored locally."
            ),
            LocaleTermsSection(
                title = "3. Location Data Usage for Educational Verification",
                content = "Location data is utilized exclusively on-device to confirm the learner's home country and lock the curriculum to their national standards. Location data is never transmitted to remote tracking servers."
            ),
            LocaleTermsSection(
                title = "4. Internet Access & Cloud AI Notice",
                content = "Internet access is used solely to connect to Cloud AI services for real-time speech interaction and curriculum synchronization."
            ),
            LocaleTermsSection(
                title = "5. Artificial Intelligence Mistakes & Supervision",
                content = "AI models may make mistakes, just like any AI tool. Active parental supervision is recommended."
            )
        )
    )
}
