package com.example.data.model

data class EducationalLocale(
    val country: String,
    val countryCode: String,
    val flagEmoji: String,
    val stateOrProvince: String,
    val city: String,
    val schoolDistrict: String,
    val standardTitle: String,
    val description: String
) {
    val countryName: String get() = country
    val defaultStateOrProvince: String get() = stateOrProvince
    val defaultCity: String get() = city
    val schoolDistricts: List<String> get() = listOf(schoolDistrict)
    val stateCurriculumStandards: List<String> get() = listOf(standardTitle)
    val primaryLanguageCode: String get() = when (countryCode.uppercase()) {
        "GB" -> "en-GB"
        "ES", "MX", "CO", "AR", "CL" -> "es"
        "FR" -> "fr"
        "DE", "AT" -> "de"
        "JP" -> "ja"
        "BR", "PT" -> "pt"
        "IN" -> "hi"
        "CN", "TW" -> "zh"
        "SA", "AE" -> "ar"
        "IT" -> "it"
        "KR" -> "ko"
        "NL" -> "nl"
        "PL" -> "pl"
        "SE" -> "sv"
        "TR" -> "tr"
        "VN" -> "vi"
        "TH" -> "th"
        "ID" -> "id"
        "GR" -> "el"
        else -> "en-US"
    }
}

val GLOBAL_EDUCATIONAL_LOCALES = listOf(
    // ==========================================
    // UNITED KINGDOM (England, Scotland, Wales, Northern Ireland)
    // ==========================================
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Greater London", "London", "Inner London Boroughs Education Authority", "UK National Curriculum (Key Stages 1-4)", "Department for Education (DfE) National Curriculum Standards across Key Stages"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Greater London", "Westminster", "Westminster City Council Children's Services", "UK National Curriculum (Key Stages 1-4)", "DfE Central London Core Educational Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Greater London", "Camden", "Camden Local Education Authority (LEA)", "UK National Curriculum (Key Stages 1-4)", "Camden Borough Primary & Secondary Curriculum Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Greater London", "Greenwich", "Royal Borough of Greenwich Education", "UK National Curriculum (Key Stages 1-4)", "Greenwich Standards and School Achievement"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Greater London", "Croydon", "Croydon Council Education & Learning", "UK National Curriculum (Key Stages 1-4)", "South London Local Education Authority Guidelines"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Greater London", "Barnet", "Barnet Education and Learning Service (BELS)", "UK National Curriculum (Key Stages 1-4)", "North London Academic Excellence Guidelines"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - North West", "Manchester", "Manchester City Council Learning & Schools", "UK National Curriculum (Key Stages 1-4)", "Greater Manchester Combined Authority Primary Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - North West", "Liverpool", "Liverpool City Council Education Services", "UK National Curriculum (Key Stages 1-4)", "Merseyside Primary and Secondary Learning Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - North West", "Salford", "Salford City Council Learning Directorate", "UK National Curriculum (Key Stages 1-4)", "Salford Early Years & Primary Education Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - West Midlands", "Birmingham", "Birmingham City Council Education & Skills", "UK National Curriculum (Key Stages 1-4)", "Birmingham Children's Trust & DfE Curriculum"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - West Midlands", "Coventry", "Coventry City Local Education Authority", "UK National Curriculum (Key Stages 1-4)", "Coventry Primary & Secondary Academic Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - West Midlands", "Wolverhampton", "City of Wolverhampton Council Education", "UK National Curriculum (Key Stages 1-4)", "Black Country Comprehensive Learning Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Yorkshire & the Humber", "Leeds", "Leeds City Council Learning Improvement", "UK National Curriculum (Key Stages 1-4)", "Leeds Learning Partnership & Core Competencies"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Yorkshire & the Humber", "Sheffield", "Sheffield City Council Education Directorate", "UK National Curriculum (Key Stages 1-4)", "South Yorkshire Regional Learning Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - Yorkshire & the Humber", "York", "City of York Council Education & Skills", "UK National Curriculum (Key Stages 1-4)", "York Primary & Secondary Academic Excellence"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - South West", "Bristol", "Bristol City Council Education & Learning", "UK National Curriculum (Key Stages 1-4)", "Bristol Learning City Initiative & DfE Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - South West", "Bath", "Bath & North East Somerset Education", "UK National Curriculum (Key Stages 1-4)", "BANES Primary & Key Stage Education Guidelines"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - South West", "Plymouth", "Plymouth City Council Education Directorate", "UK National Curriculum (Key Stages 1-4)", "Devon & Cornwall Regional Education Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - South East", "Oxford", "Oxfordshire County Council Education LEA", "UK National Curriculum (Key Stages 1-4)", "Oxfordshire Primary & Secondary Schools Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - South East", "Brighton", "Brighton & Hove City Council Education", "UK National Curriculum (Key Stages 1-4)", "Sussex Coastal Learning Community Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - South East", "Southampton", "Southampton City Council Learning Services", "UK National Curriculum (Key Stages 1-4)", "Hampshire & Solent Academic Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - South East", "Milton Keynes", "Milton Keynes Council Education Department", "UK National Curriculum (Key Stages 1-4)", "MK Schools Learning & STEM Innovation Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - East Midlands", "Nottingham", "Nottingham City Council Education LEA", "UK National Curriculum (Key Stages 1-4)", "East Midlands Regional Learning Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - East Midlands", "Leicester", "Leicester City Council Learning Directorate", "UK National Curriculum (Key Stages 1-4)", "Leicester Diverse & Inclusive Academic Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - East of England", "Cambridge", "Cambridgeshire County Council LEA", "UK National Curriculum (Key Stages 1-4)", "Cambridgeshire STEM & Primary Academic Framework"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - East of England", "Norwich", "Norfolk County Council Children's Services", "UK National Curriculum (Key Stages 1-4)", "Norfolk Primary Learning & Key Stages Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "England - North East", "Newcastle upon Tyne", "Newcastle City Council Learning & Schools", "UK National Curriculum (Key Stages 1-4)", "Tyne and Wear Regional Education Directorate"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "Scotland", "Edinburgh", "City of Edinburgh Council Education Department", "Scotland Curriculum for Excellence (CfE)", "Education Scotland Curriculum for Excellence (CfE) Levels & Benchmarks"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "Scotland", "Glasgow", "Glasgow City Council Education Services", "Scotland Curriculum for Excellence (CfE)", "Glasgow Schools CfE Broad General Education Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "Scotland", "Aberdeen", "Aberdeen City Council Education Services", "Scotland Curriculum for Excellence (CfE)", "Grampian Region Primary & Secondary Curriculum"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "Wales", "Cardiff", "Cardiff Council Education Services", "Curriculum for Wales (Cwricwlwm i Gymru)", "Welsh Government 4 Core Purposes & Progression Steps"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "Wales", "Swansea", "Swansea Council Education Department", "Curriculum for Wales (Cwricwlwm i Gymru)", "South West Wales Regional Education Consortium Standards"),
    EducationalLocale("United Kingdom", "GB", "🇬🇧", "Northern Ireland", "Belfast", "Education Authority Northern Ireland (EA) - Belfast Region", "Northern Ireland Curriculum (Key Stages 1-4)", "CCEA Northern Ireland Primary & Post-Primary Curriculum"),

    // ==========================================
    // UNITED STATES (Extensive states and districts)
    // ==========================================
    EducationalLocale("United States", "US", "🇺🇸", "California", "Los Angeles", "Los Angeles Unified School District (LAUSD)", "LAUSD CA-CCSS & STEAM Framework", "California Common Core State Standards & LAUSD STEAM Guidelines"),
    EducationalLocale("United States", "US", "🇺🇸", "California", "San Francisco", "San Francisco Unified School District (SFUSD)", "SFUSD Core Standards & Math Framework", "San Francisco Unified Core STEM & Equity-Focused Curriculum"),
    EducationalLocale("United States", "US", "🇺🇸", "California", "San Diego", "San Diego Unified School District (SDUSD)", "CA-CCSS & NextGen Science (NGSS)", "San Diego Unified Integrated STEAM Curriculum"),
    EducationalLocale("United States", "US", "🇺🇸", "California", "San Jose", "San Jose Unified School District (SJUSD)", "Silicon Valley STEM & CA-CCSS", "Santa Clara County Academic Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "California", "Sacramento", "Sacramento City Unified School District (SCUSD)", "California State Board of Education Standards", "Sacramento Capital Region Educational Guidelines"),
    EducationalLocale("United States", "US", "🇺🇸", "New York", "New York City", "NYC Department of Education (NYCDOE)", "NYS Next Generation Learning Standards", "New York State P-12 Next Generation ELA and Mathematics Learning Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "New York", "Buffalo", "Buffalo Public Schools (District 6)", "NYS Next Gen & Buffalo STEAM Standards", "Western New York Learning & Achievement Benchmarks"),
    EducationalLocale("United States", "US", "🇺🇸", "New York", "Rochester", "Rochester City School District (RCSD)", "NYS Next Generation Standards", "Monroe County Primary Learning Framework"),
    EducationalLocale("United States", "US", "🇺🇸", "Texas", "Houston", "Houston Independent School District (HISD)", "TEKS (Texas Essential Knowledge and Skills)", "Texas State Board of Education Essential Knowledge & Skills"),
    EducationalLocale("United States", "US", "🇺🇸", "Texas", "Dallas", "Dallas Independent School District (DISD)", "TEKS & Dallas Core STEM Guidelines", "Dallas County Academic Achievement Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "Texas", "Austin", "Austin Independent School District (AISD)", "TEKS & AISD Creative Learning Framework", "Central Texas Essential Knowledge & Skills Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "Texas", "San Antonio", "Northside Independent School District (NISD)", "TEKS & Bexar County Academic Framework", "San Antonio Area Elementary Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "Florida", "Miami", "Miami-Dade County Public Schools (M-DCPS)", "Florida B.E.S.T. Standards", "Florida Benchmarks for Excellent Student Thinking (B.E.S.T.) Framework"),
    EducationalLocale("United States", "US", "🇺🇸", "Florida", "Orlando", "Orange County Public Schools (OCPS)", "Florida B.E.S.T. & OCPS STEM", "Central Florida Elementary & Secondary Curriculum"),
    EducationalLocale("United States", "US", "🇺🇸", "Florida", "Tampa", "Hillsborough County Public Schools (HCPS)", "Florida B.E.S.T. Framework", "Tampa Bay Academic Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "Illinois", "Chicago", "Chicago Public Schools (CPS)", "Illinois Learning Standards & CPS Framework", "Illinois State Learning Standards integrated with CPS Social-Emotional Learning"),
    EducationalLocale("United States", "US", "🇺🇸", "Illinois", "Naperville", "Naperville Community Unit School District 203", "Illinois State Standards & Advanced STEM", "DuPage County Academic Excellence Guidelines"),
    EducationalLocale("United States", "US", "🇺🇸", "Washington", "Seattle", "Seattle Public Schools (SPS)", "WA OSPI Learning Standards & SPS Core", "Washington State OSPI Standards with Inquiry-Based Science & Math"),
    EducationalLocale("United States", "US", "🇺🇸", "Washington", "Bellevue", "Bellevue School District (BSD 405)", "WA OSPI & Bellevue STEM Excellence", "King County Advanced Academic Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "Massachusetts", "Boston", "Boston Public Schools (BPS)", "Massachusetts Curriculum Frameworks (MCAS)", "Mass DESE World-Class Elementary & Secondary Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "Massachusetts", "Cambridge", "Cambridge Public Schools (CPSD)", "Massachusetts Frameworks & CPSD Core", "Cambridge Innovation & Math Curricula"),
    EducationalLocale("United States", "US", "🇺🇸", "Pennsylvania", "Philadelphia", "School District of Philadelphia (SDP)", "PA Core Standards & SDP Action Plan", "Pennsylvania Academic Standards for Reading & Mathematics"),
    EducationalLocale("United States", "US", "🇺🇸", "Pennsylvania", "Pittsburgh", "Pittsburgh Public Schools (PPS)", "PA Core Standards & Pittsburgh STEM", "Allegheny County Primary Learning Guidelines"),
    EducationalLocale("United States", "US", "🇺🇸", "Georgia", "Atlanta", "Atlanta Public Schools (APS)", "Georgia Standards of Excellence (GSE)", "Georgia Department of Education Core Competencies"),
    EducationalLocale("United States", "US", "🇺🇸", "Ohio", "Columbus", "Columbus City Schools (CCS)", "Ohio's Learning Standards (ODE)", "Ohio Department of Education Learning Guidelines"),
    EducationalLocale("United States", "US", "🇺🇸", "Michigan", "Detroit", "Detroit Public Schools Community District (DPSCD)", "Michigan Academic Standards (M-STEP)", "Michigan Department of Education Core Curricula"),
    EducationalLocale("United States", "US", "🇺🇸", "North Carolina", "Charlotte", "Charlotte-Mecklenburg Schools (CMS)", "NC Standard Course of Study (NCSCOS)", "North Carolina Department of Public Instruction Guidelines"),
    EducationalLocale("United States", "US", "🇺🇸", "Virginia", "Fairfax", "Fairfax County Public Schools (FCPS)", "Virginia Standards of Learning (SOL)", "Virginia Board of Education SOL & Advanced Academics"),
    EducationalLocale("United States", "US", "🇺🇸", "Colorado", "Denver", "Denver Public Schools (DPS)", "Colorado Academic Standards (CAS)", "Colorado Department of Education 21st Century Skills"),
    EducationalLocale("United States", "US", "🇺🇸", "Arizona", "Phoenix", "Phoenix Union / Phoenix Elementary District", "Arizona Academic Standards (AzM2)", "Arizona Department of Education Core Standards"),
    EducationalLocale("United States", "US", "🇺🇸", "Washington D.C.", "Washington", "District of Columbia Public Schools (DCPS)", "DCPS Learning Standards & DC-CAS", "District of Columbia Educational Guidelines"),

    // ==========================================
    // CANADA (Provinces & Major School Boards)
    // ==========================================
    EducationalLocale("Canada", "CA", "🇨🇦", "Ontario", "Toronto", "Toronto District School Board (TDSB)", "Ontario Curriculum & TDSB Framework", "Ontario Ministry of Education Elementary & Secondary Curriculum"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Ontario", "Ottawa", "Ottawa-Carleton District School Board (OCDSB)", "Ontario Curriculum Standards", "Capital Region Bilingual & STEM Core Guidelines"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Ontario", "Mississauga", "Peel District School Board (PDSB)", "Ontario Curriculum & Peel STEAM", "Greater Toronto Region Academic Standards"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Quebec", "Montreal", "Centre de services scolaire de Montréal (CSSDM)", "PFEQ (Programme de Formation Québécoise)", "Ministère de l'Éducation du Québec - Competency-Based Learning"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Quebec", "Quebec City", "Centre de services scolaire de la Capitale", "PFEQ Standards du Québec", "Québec Capitale-Nationale Academic Framework"),
    EducationalLocale("Canada", "CA", "🇨🇦", "British Columbia", "Vancouver", "Vancouver School Board (SD39)", "BC Building Student Success Curriculum", "BC Ministry of Education Concept-Based Competency-Driven Curriculum"),
    EducationalLocale("Canada", "CA", "🇨🇦", "British Columbia", "Victoria", "Greater Victoria School District (SD61)", "BC Core Competencies Framework", "Vancouver Island Primary Educational Standards"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Alberta", "Calgary", "Calgary Board of Education (CBE)", "Alberta Programs of Study (K-12)", "Alberta Education Competency-Based Curriculum Framework"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Alberta", "Edmonton", "Edmonton Public Schools (EPSB)", "Alberta Programs of Study", "Edmonton Metro Learning Standards"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Manitoba", "Winnipeg", "Winnipeg School Division (WSD)", "Manitoba Curriculum Framework", "Manitoba Education and Early Childhood Learning Standards"),
    EducationalLocale("Canada", "CA", "🇨🇦", "Nova Scotia", "Halifax", "Halifax Regional Centre for Education (HRCE)", "Nova Scotia Public School Program (PSP)", "Atlantic Canada Core Educational Guidelines"),

    // ==========================================
    // AUSTRALIA (All States & Territories)
    // ==========================================
    EducationalLocale("Australia", "AU", "🇦🇺", "New South Wales", "Sydney", "NSW Dept of Education - Sydney Metro", "NSW Curriculum & NESA Syllabuses", "NSW Education Standards Authority (NESA) Syllabuses & Capabilities"),
    EducationalLocale("Australia", "AU", "🇦🇺", "New South Wales", "Newcastle", "NSW Dept of Education - Hunter Region", "NSW NESA Curriculum", "Hunter & Central Coast Academic Guidelines"),
    EducationalLocale("Australia", "AU", "🇦🇺", "Victoria", "Melbourne", "Victorian Dept of Education - Melbourne Metro", "Victorian Curriculum (F-10)", "VCAA Victorian Curriculum Foundation to Year 10 Framework"),
    EducationalLocale("Australia", "AU", "🇦🇺", "Victoria", "Geelong", "Victorian Dept of Education - Barwon Region", "Victorian Curriculum (F-10)", "VCAA Regional Learning Guidelines"),
    EducationalLocale("Australia", "AU", "🇦🇺", "Queensland", "Brisbane", "Education Queensland - Metropolitan Region", "Australian Curriculum (ACARA) & QCAA", "Queensland Curriculum & Assessment Authority Framework"),
    EducationalLocale("Australia", "AU", "🇦🇺", "Western Australia", "Perth", "WA Dept of Education - North Metro", "Western Australian Curriculum (SCSA)", "School Curriculum and Standards Authority Guidelines"),
    EducationalLocale("Australia", "AU", "🇦🇺", "South Australia", "Adelaide", "South Australian Dept for Education", "South Australian Curriculum & SACE", "SA Primary & Middle Years Learning Framework"),
    EducationalLocale("Australia", "AU", "🇦🇺", "Australian Capital Territory", "Canberra", "ACT Education Directorate", "Australian Curriculum in ACT Schools", "ACT Government Quality Public Education Guidelines"),
    EducationalLocale("Australia", "AU", "🇦🇺", "Tasmania", "Hobart", "Tasmanian Dept for Education, Children and Young People", "Australian Curriculum in Tasmania", "Tasmanian Learning Standards"),

    // ==========================================
    // IRELAND
    // ==========================================
    EducationalLocale("Ireland", "IE", "🇮🇪", "Leinster", "Dublin", "Dublin and Dún Laoghaire ETB (DDLETB)", "Irish Primary School Curriculum (NCCA)", "National Council for Curriculum and Assessment Primary Framework"),
    EducationalLocale("Ireland", "IE", "🇮🇪", "Munster", "Cork", "Cork Education and Training Board (CETB)", "NCCA Primary & Junior Cycle Framework", "Department of Education and Skills Standards"),
    EducationalLocale("Ireland", "IE", "🇮🇪", "Connacht", "Galway", "Galway and Roscommon ETB (GRETB)", "Irish National Curriculum (NCCA)", "Western Region Primary Educational Guidelines"),

    // ==========================================
    // NEW ZEALAND
    // ==========================================
    EducationalLocale("New Zealand", "NZ", "🇳🇿", "Auckland Region", "Auckland", "Auckland Central Ministry of Education Region", "The New Zealand Curriculum (Te Marautanga o Aotearoa)", "NZ Ministry of Education Key Competencies & Learning Areas"),
    EducationalLocale("New Zealand", "NZ", "🇳🇿", "Wellington Region", "Wellington", "Wellington Regional Education Directorate", "The New Zealand Curriculum", "Capital Region Primary and Intermediate Framework"),
    EducationalLocale("New Zealand", "NZ", "🇳🇿", "Canterbury", "Christchurch", "Canterbury Regional Education Office", "The New Zealand Curriculum", "South Island Core Educational Standards"),

    // ==========================================
    // GERMANY (Bundesländer)
    // ==========================================
    EducationalLocale("Germany", "DE", "🇩🇪", "Bavaria (Bayern)", "Munich (München)", "Stadt München Schulreferat", "Bayerischer LehrplanPLUS", "Bayerisches Staatsministerium für Unterricht und Kultus LehrplanPLUS"),
    EducationalLocale("Germany", "DE", "🇩🇪", "Bavaria (Bayern)", "Nuremberg (Nürnberg)", "Staatliches Schulamt Nürnberg", "Bayerischer LehrplanPLUS", "Mittelfranken Bildungsstandards"),
    EducationalLocale("Germany", "DE", "🇩🇪", "Berlin", "Berlin", "Senatsverwaltung für Bildung, Jugend und Familie", "Berliner Rahmenlehrplan", "Gemeinsamer Rahmenlehrplan für die Länder Berlin und Brandenburg"),
    EducationalLocale("Germany", "DE", "🇩🇪", "Baden-Württemberg", "Stuttgart", "Staatliches Schulamt Stuttgart", "Bildungsplan Baden-Württemberg", "Ministerium für Kultus, Jugend und Sport BW Standards"),
    EducationalLocale("Germany", "DE", "🇩🇪", "North Rhine-Westphalia (NRW)", "Cologne (Köln)", "Schulamt für die Stadt Köln", "Kernlehrpläne NRW", "Ministerium für Schule und Bildung NRW Richtlinien"),
    EducationalLocale("Germany", "DE", "🇩🇪", "North Rhine-Westphalia (NRW)", "Düsseldorf", "Schulamt Düsseldorf", "Kernlehrpläne NRW", "Landeshauptstadt Düsseldorf Bildungsstandards"),
    EducationalLocale("Germany", "DE", "🇩🇪", "Hesse (Hessen)", "Frankfurt am Main", "Staatliches Schulamt Frankfurt", "Kerncurriculum Hessen (KCH)", "Hessisches Kultusministerium Bildungsstandards"),
    EducationalLocale("Germany", "DE", "🇩🇪", "Hamburg", "Hamburg", "Behörde für Schule und Berufsbildung Hamburg", "Hamburger Bildungspläne", "Freie und Hansestadt Hamburg Bildungsstandards"),

    // ==========================================
    // FRANCE (Académies)
    // ==========================================
    EducationalLocale("France", "FR", "🇫🇷", "Île-de-France", "Paris", "Académie de Paris", "Socle Commun de Connaissances et Compétences", "Ministère de l'Éducation Nationale - Programmes Cycle 1-4"),
    EducationalLocale("France", "FR", "🇫🇷", "Île-de-France", "Versailles", "Académie de Versailles", "Socle Commun de l'Éducation Nationale", "Région Parisienne Ouest Standards Académiques"),
    EducationalLocale("France", "FR", "🇫🇷", "Auvergne-Rhône-Alpes", "Lyon", "Académie de Lyon", "Programmes Scolaires de l'Éducation Nationale", "Rectorat de Lyon - Normes et Compétences"),
    EducationalLocale("France", "FR", "🇫🇷", "Provence-Alpes-Côte d'Azur", "Marseille", "Académie d'Aix-Marseille", "Socle Commun & Programmes Élémentaires", "Région Sud Référentiel Pédagogique"),
    EducationalLocale("France", "FR", "🇫🇷", "Nouvelle-Aquitaine", "Bordeaux", "Académie de Bordeaux", "Programmes de l'Éducation Nationale", "Nouvelle-Aquitaine Cadre Pédagogique"),

    // ==========================================
    // JAPAN (Prefectures & Cities)
    // ==========================================
    EducationalLocale("Japan", "JP", "🇯🇵", "Tokyo Metropolis", "Tokyo (23 Wards)", "Tokyo Metropolitan Board of Education", "MEXT Curriculum Guidelines (Gakushu Shidou Yoryo)", "Ministry of Education, Culture, Sports, Science and Technology (MEXT) Standards"),
    EducationalLocale("Japan", "JP", "🇯🇵", "Osaka Prefecture", "Osaka", "Osaka City Board of Education", "MEXT Elementary & Junior High Guidelines", "Osaka Municipal Academic & Moral Education Standards"),
    EducationalLocale("Japan", "JP", "🇯🇵", "Kanagawa Prefecture", "Yokohama", "Yokohama City Board of Education", "MEXT Curriculum Standards", "Yokohama City Primary Learning Program"),
    EducationalLocale("Japan", "JP", "🇯🇵", "Aichi Prefecture", "Nagoya", "Nagoya City Board of Education", "MEXT Curriculum Standards", "Chubu Region Elementary Guidelines"),
    EducationalLocale("Japan", "JP", "🇯🇵", "Kyoto Prefecture", "Kyoto", "Kyoto City Board of Education", "MEXT Standards & Kyoto Heritage Learning", "Kyoto Municipal Academic Framework"),

    // ==========================================
    // INDIA (Boards & States)
    // ==========================================
    EducationalLocale("India", "IN", "🇮🇳", "Maharashtra", "Mumbai", "BMC Education Dept & CBSE/ICSE Board", "NCERT / CBSE National Curriculum Framework (NCF)", "National Education Policy (NEP) & CBSE Board Competency Framework"),
    EducationalLocale("India", "IN", "🇮🇳", "Maharashtra", "Pune", "PMC Education Department & State Board", "Maharashtra State Board (MSBSHSE) & NCF", "Pune District Primary Curriculum Standards"),
    EducationalLocale("India", "IN", "🇮🇳", "Delhi NCR", "New Delhi", "Directorate of Education Delhi & CBSE", "Delhi State & CBSE Academic Standards", "NCERT Core Syllabus with Happiness & STEM Innovation Curricula"),
    EducationalLocale("India", "IN", "🇮🇳", "Karnataka", "Bengaluru", "Karnataka Dept of School Education & CBSE", "Karnataka State Board (KSEEB) & CBSE", "Bangalore Urban Primary Learning Framework"),
    EducationalLocale("India", "IN", "🇮🇳", "Tamil Nadu", "Chennai", "Chennai Corporation Education & Samacheer Kalvi", "Tamil Nadu State Board & CBSE", "Samacheer Kalvi Curriculum Framework"),
    EducationalLocale("India", "IN", "🇮🇳", "Telangana", "Hyderabad", "Telangana School Education Dept & CBSE", "Telangana State Board (SCERT) & CBSE", "Hyderabad Metro Academic Guidelines"),

    // ==========================================
    // BRAZIL
    // ==========================================
    EducationalLocale("Brazil", "BR", "🇧🇷", "São Paulo", "São Paulo", "Secretaria Municipal de Educação de São Paulo (SME-SP)", "BNCC (Base Nacional Comum Curricular)", "MEC Base Nacional Comum Curricular & Currículo da Cidade de São Paulo"),
    EducationalLocale("Brazil", "BR", "🇧🇷", "Rio de Janeiro", "Rio de Janeiro", "Secretaria Municipal de Educação do Rio (SME-Rio)", "BNCC & Currículo Carioca", "Rio de Janeiro Diretrizes Curriculares da Educação Básica"),
    EducationalLocale("Brazil", "BR", "🇧🇷", "Minas Gerais", "Belo Horizonte", "Secretaria Municipal de Educação (SMED-BH)", "BNCC & ProBNCC Minas Gerais", "Currículo Referência de Minas Gerais"),

    // ==========================================
    // MEXICO
    // ==========================================
    EducationalLocale("Mexico", "MX", "🇲🇽", "Ciudad de México", "Mexico City", "Autoridad Educativa Federal en la CDMX (AEFCM)", "Plan de Estudios de la Nueva Escuela Mexicana (SEP)", "SEP Programas de Estudio Educación Básica de la Nueva Escuela Mexicana"),
    EducationalLocale("Mexico", "MX", "🇲🇽", "Jalisco", "Guadalajara", "Secretaría de Educación Jalisco (SEJ)", "Recrea Educación para la Vida (SEP/Jalisco)", "Jalisco Modelo Educativo y Currículo Integral"),
    EducationalLocale("Mexico", "MX", "🇲🇽", "Nuevo León", "Monterrey", "Secretaría de Educación de Nuevo León", "SEP & Currículo Nuevo León", "Monterrey Competencias Básicas y Aprendizajes Clave"),

    // ==========================================
    // SPAIN (Comunidades Autónomas)
    // ==========================================
    EducationalLocale("Spain", "ES", "🇪🇸", "Comunidad de Madrid", "Madrid", "Consejería de Educación, Ciencia y Universidades", "LOMLOE & Currículo de Primaria de la Comunidad de Madrid", "Ministerio de Educación y Formación Profesional (LOMLOE)"),
    EducationalLocale("Spain", "ES", "🇪🇸", "Cataluña", "Barcelona", "Consorci d'Educació de Barcelona", "Currículum d'Educació Primària (Departament d'Educació)", "Generalitat de Catalunya Competències Bàsiques"),
    EducationalLocale("Spain", "ES", "🇪🇸", "Andalucía", "Sevilla", "Consejería de Desarrollo Educativo y FP", "Currículo de Primaria de Andalucía (LOMLOE)", "Junta de Andalucía Marco Educativo de Primaria"),
    EducationalLocale("Spain", "ES", "🇪🇸", "Comunidad Valenciana", "Valencia", "Conselleria d'Educació de la Generalitat Valenciana", "Currículum d'Educació Primària Valenciana", "Generalitat Valenciana Competències Clau"),

    // ==========================================
    // ITALY (Regioni)
    // ==========================================
    EducationalLocale("Italy", "IT", "🇮🇹", "Lombardia", "Milano", "Ufficio Scolastico Regionale per la Lombardia (Milano)", "Indicazioni Nazionali per il Curricolo (MIUR)", "Ministero dell'Istruzione e del Merito Competenze Chiave"),
    EducationalLocale("Italy", "IT", "🇮🇹", "Lazio", "Roma", "Ufficio Scolastico Regionale per il Lazio (Roma)", "Indicazioni Nazionali per il Curricolo", "Roma Capitale Linee Guida per la Scuola Primaria"),
    EducationalLocale("Italy", "IT", "🇮🇹", "Piemonte", "Torino", "Ufficio Scolastico Regionale Piemonte", "Indicazioni Nazionali MIUR", "Piemonte Standard Educativi per la Scuola Primaria"),

    // ==========================================
    // NETHERLANDS
    // ==========================================
    EducationalLocale("Netherlands", "NL", "🇳🇱", "North Holland", "Amsterdam", "Gemeente Amsterdam Onderwijs & SLO", "Kerndoelen Primair Onderwijs (SLO)", "Ministerie van Onderwijs, Cultuur en Wetenschap (OCW) Kerndoelen"),
    EducationalLocale("Netherlands", "NL", "🇳🇱", "South Holland", "Rotterdam", "Gemeente Rotterdam Onderwijs & Besturen", "Kerndoelen Primair Onderwijs", "Rotterdam Educatieve Basisdoelen"),
    EducationalLocale("Netherlands", "NL", "🇳🇱", "Utrecht", "Utrecht", "Gemeente Utrecht Afdeling Onderwijs", "SLO Kerndoelen & Dalton/Montessori Standards", "Midden-Nederland Onderwijsdoelen"),

    // ==========================================
    // SWEDEN, NORWAY, DENMARK, FINLAND
    // ==========================================
    EducationalLocale("Sweden", "SE", "🇸🇪", "Stockholm County", "Stockholm", "Stockholms Stads Utbildningsförvaltning", "Lgr22 (Läroplan för grundskolan)", "Skolverket National Curriculum for Compulsory School"),
    EducationalLocale("Norway", "NO", "🇳🇴", "Oslo", "Oslo", "Utdanningsetaten i Oslo", "LK20 (Kunnskapsløftet 2020)", "Utdanningsdirektoratet Core Curriculum Standards"),
    EducationalLocale("Denmark", "DK", "🇩🇰", "Capital Region", "Copenhagen", "Københavns Kommune Børne- og Ungdomsforvaltningen", "Fælles Mål (Folkeskolen)", "Børne- og Undervisningsministeriet Common Objectives"),
    EducationalLocale("Finland", "FI", "🇫🇮", "Uusimaa", "Helsinki", "Helsinki Education Division (Kasvatus ja koulutus)", "Finnish National Core Curriculum (OPS 2016)", "Finnish National Agency for Education (Opetushallitus) World-Leading Standards"),

    // ==========================================
    // SWITZERLAND & AUSTRIA
    // ==========================================
    EducationalLocale("Switzerland", "CH", "🇨🇭", "Zurich", "Zürich", "Volksschulamt Kanton Zürich", "Lehrplan 21 (Deutschschweiz)", "D-EDK Deutschschweizer Erziehungsdirektoren-Konferenz"),
    EducationalLocale("Switzerland", "CH", "🇨🇭", "Geneva", "Geneva (Genève)", "Département de l'Instruction Publique (DIP)", "Plan d'Études Romand (PER)", "Conférence Intercantonale de l'Instruction Publique (CIIP)"),
    EducationalLocale("Austria", "AT", "🇦🇹", "Vienna", "Vienna (Wien)", "Bildungsdirektion für Wien", "Österreichischer Lehrplan der Volksschule", "Bundesministerium für Bildung, Wissenschaft und Forschung (BMBWF)"),

    // ==========================================
    // SOUTH KOREA & SINGAPORE
    // ==========================================
    EducationalLocale("South Korea", "KR", "🇰🇷", "Seoul Capital Area", "Seoul", "Seoul Metropolitan Office of Education (SMOE)", "National Curriculum of Korea (MOE Korea)", "Ministry of Education Korea Elementary Standards"),
    EducationalLocale("South Korea", "KR", "🇰🇷", "Gyeonggi Province", "Suwon", "Gyeonggi Provincial Office of Education (GOE)", "National Curriculum & Gyeonggi Creative Framework", "Gyeonggi Elementary Learning Guidelines"),
    EducationalLocale("Singapore", "SG", "🇸🇬", "Central Region", "Singapore", "Ministry of Education Singapore (MOE)", "Singapore MOE Primary Curriculum & Syllabuses", "MOE Singapore 21st Century Competencies Framework"),

    // ==========================================
    // UNITED ARAB EMIRATES & SAUDI ARABIA
    // ==========================================
    EducationalLocale("United Arab Emirates", "AE", "🇦🇪", "Dubai", "Dubai", "Knowledge and Human Development Authority (KHDA)", "UAE Ministry of Education (MOE) & KHDA Standards", "UAE National Curriculum & International Frameworks"),
    EducationalLocale("United Arab Emirates", "AE", "🇦🇪", "Abu Dhabi", "Abu Dhabi", "Abu Dhabi Dept of Education and Knowledge (ADEK)", "ADEK Curriculum Framework & UAE MOE", "Abu Dhabi Educational Guidelines"),
    EducationalLocale("Saudi Arabia", "SA", "🇸🇦", "Riyadh Province", "Riyadh", "Ministry of Education - Riyadh Education Directorate", "Saudi National Curriculum (Vision 2030 Standards)", "Ministry of Education Saudi Arabia Primary Standards"),

    // ==========================================
    // SOUTH AFRICA
    // ==========================================
    EducationalLocale("South Africa", "ZA", "🇿🇦", "Gauteng", "Johannesburg", "Gauteng Department of Education (GDE)", "CAPS (Curriculum and Assessment Policy Statement)", "Department of Basic Education South Africa CAPS Framework"),
    EducationalLocale("South Africa", "ZA", "🇿🇦", "Western Cape", "Cape Town", "Western Cape Education Department (WCED)", "CAPS Curriculum & WCED Framework", "Western Cape Primary Learning Guidelines"),

    // ==========================================
    // ADDITIONAL WORLD REGIONS
    // ==========================================
    EducationalLocale("Poland", "PL", "🇵🇱", "Mazovia", "Warsaw", "Biuro Edukacji m.st. Warszawy", "Podstawa Programowa Kształcenia Ogólnego", "Ministerstwo Edukacji Narodowej (MEN) Podstawa Programowa"),
    EducationalLocale("Portugal", "PT", "🇵🇹", "Lisbon District", "Lisbon", "Direção-Geral da Educação (DGE Portugal)", "Aprendizagens Essenciais do Ensino Básico", "Ministério da Educação de Portugal Diretrizes"),
    EducationalLocale("Greece", "GR", "🇬🇷", "Attica", "Athens", "Regional Directorate of Primary Education of Attica", "New Curriculum for Primary Schools (IEP)", "Institute of Educational Policy (IEP) & Ministry of Education"),
    EducationalLocale("Chile", "CL", "🇨🇱", "Región Metropolitana", "Santiago", "Ministerio de Educación de Chile (Mineduc)", "Bases Curriculares de Educación Básica (Mineduc)", "Consejo Nacional de Educación de Chile"),
    EducationalLocale("Colombia", "CO", "🇨🇴", "Bogotá D.C.", "Bogotá", "Secretaría de Educación del Distrito (SED Bogotá)", "Estándares Básicos de Competencias (MEN Colombia)", "Ministerio de Educación Nacional de Colombia"),
    EducationalLocale("Argentina", "AR", "🇦🇷", "Buenos Aires", "Buenos Aires", "Ministerio de Educación de la Ciudad de Buenos Aires", "Diseño Curricular para la Educación Primaria (NAP)", "Núcleos de Aprendizajes Prioritarios (NAP Argentina)"),
    EducationalLocale("Philippines", "PH", "🇵🇭", "National Capital Region", "Manila", "Department of Education (DepEd) NCR", "MATATAG K to 10 Curriculum & DepEd Standards", "Department of Education Philippines Standards"),
    EducationalLocale("Malaysia", "MY", "🇲🇾", "Kuala Lumpur", "Kuala Lumpur", "Jabatan Pendidikan Wilayah Persekutuan Kuala Lumpur", "KSSR (Kurikulum Standard Sekolah Rendah)", "Kementerian Pendidikan Malaysia (KPM) Standards"),
    EducationalLocale("Thailand", "TH", "🇹🇭", "Bangkok", "Bangkok", "Office of the Basic Education Commission (OBEC)", "Basic Education Core Curriculum B.E. 2551 (A.D. 2008)", "Ministry of Education Thailand Guidelines"),
    EducationalLocale("Indonesia", "ID", "🇮🇩", "DKI Jakarta", "Jakarta", "Dinas Pendidikan Provinsi DKI Jakarta", "Kurikulum Merdeka (Kemendikbudristek)", "Kementerian Pendidikan, Kebudayaan, Riset, dan Teknologi Indonesia"),
    EducationalLocale("Vietnam", "VN", "🇻🇳", "Hanoi", "Hanoi", "Hanoi Department of Education and Training (DOET)", "General Education Program 2018 (MOET Vietnam)", "Ministry of Education and Training Vietnam Competencies"),
    EducationalLocale("Taiwan", "TW", "🇹🇼", "Taipei", "Taipei City", "Taipei City Department of Education", "Curriculum Guidelines for 12-Year Basic Education", "Ministry of Education (Taiwan) Core Competency Framework")
)

object EducationalLocaleManager {
    fun getCountries(): List<String> {
        return GLOBAL_EDUCATIONAL_LOCALES.map { it.country }.distinct().sorted()
    }

    fun getStatesForCountry(country: String): List<String> {
        val matches = GLOBAL_EDUCATIONAL_LOCALES
            .filter { it.country.equals(country, ignoreCase = true) }
            .map { it.stateOrProvince }
            .distinct()
            .sorted()
        return if (matches.isNotEmpty()) matches else listOf("General / State Province")
    }

    fun getCitiesForState(country: String, state: String): List<String> {
        val matches = GLOBAL_EDUCATIONAL_LOCALES
            .filter { it.country.equals(country, ignoreCase = true) && (it.stateOrProvince.equals(state, ignoreCase = true) || state.isBlank()) }
            .map { it.city }
            .distinct()
            .sorted()
        return if (matches.isNotEmpty()) matches else listOf("Capital / Metro City")
    }

    fun getDistrictsForCity(country: String, state: String, city: String): List<EducationalLocale> {
        val matches = GLOBAL_EDUCATIONAL_LOCALES
            .filter { 
                it.country.equals(country, ignoreCase = true) && 
                (it.stateOrProvince.equals(state, ignoreCase = true) || state.isBlank()) &&
                (it.city.equals(city, ignoreCase = true) || city.isBlank())
            }
        return if (matches.isNotEmpty()) matches else listOf(
            EducationalLocale(
                country = country,
                countryCode = "GL",
                flagEmoji = "🌍",
                stateOrProvince = state.ifBlank { "General Region" },
                city = city.ifBlank { "Metro Area" },
                schoolDistrict = "Regional Public School District",
                standardTitle = "National Ministry of Education Core Guidelines",
                description = "Standardized national primary and secondary education curriculum."
            )
        )
    }

    fun findLocale(country: String, state: String, city: String, district: String): EducationalLocale? {
        return GLOBAL_EDUCATIONAL_LOCALES.find {
            it.country.equals(country, ignoreCase = true) &&
            it.stateOrProvince.equals(state, ignoreCase = true) &&
            it.city.equals(city, ignoreCase = true) &&
            it.schoolDistrict.contains(district, ignoreCase = true)
        } ?: GLOBAL_EDUCATIONAL_LOCALES.find {
            it.country.equals(country, ignoreCase = true)
        }
    }
}
