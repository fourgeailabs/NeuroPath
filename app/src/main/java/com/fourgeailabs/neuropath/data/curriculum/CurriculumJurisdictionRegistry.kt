package com.fourgeailabs.neuropath.data.curriculum

/**
 * Global curriculum routing contract.
 *
 * NeuroPath must never choose curriculum from age alone. A learner's education
 * jurisdiction is resolved first, then the learner's stage/grade and subject
 * are mapped to that jurisdiction's official framework.
 *
 * This registry is intentionally source-oriented rather than pretending there is
 * one universal curriculum. Country/territory-specific adapters can be added as
 * official sources are verified.
 */
object CurriculumJurisdictionRegistry {
    data class Jurisdiction(
        val id: String,
        val countryOrTerritory: String,
        val educationAuthority: String,
        val officialCurriculumUrl: String,
        val parentJurisdiction: String? = null,
        val notes: String = ""
    )

    /** Seeded global routing catalog. The official source remains authoritative. */
    val jurisdictions: List<Jurisdiction> = listOf(
        Jurisdiction("US", "United States", "State / local education authority", "https://www.ed.gov/"),
        Jurisdiction("CA", "Canada", "Province / territory education authority", "https://www.canada.ca/en/services/education.html"),
        Jurisdiction("AU", "Australia", "State / territory education authority", "https://www.education.gov.au/"),
        Jurisdiction("NZ", "New Zealand", "Ministry of Education", "https://www.education.govt.nz/"),
        Jurisdiction("UK", "United Kingdom", "Devolved national education authorities", "https://www.gov.uk/national-curriculum"),
        Jurisdiction("IE", "Ireland", "Department of Education", "https://www.gov.ie/en/organisation/department-of-education/"),
        Jurisdiction("ZA", "South Africa", "Department of Basic Education", "https://www.education.gov.za/"),
        Jurisdiction("IN", "India", "National and state/UT education authorities", "https://www.education.gov.in/"),
        Jurisdiction("SG", "Singapore", "Ministry of Education", "https://www.moe.gov.sg/"),
        Jurisdiction("JP", "Japan", "Ministry of Education, Culture, Sports, Science and Technology", "https://www.mext.go.jp/en/"),
        Jurisdiction("KR", "South Korea", "Ministry of Education", "https://english.moe.go.kr/"),
        Jurisdiction("FR", "France", "Ministry of National Education", "https://www.education.gouv.fr/"),
        Jurisdiction("DE", "Germany", "Federal states (Länder) education authorities", "https://www.kmk.org/"),
        Jurisdiction("ES", "Spain", "National and autonomous-community education authorities", "https://www.educacionfpydeportes.gob.es/"),
        Jurisdiction("IT", "Italy", "Ministry of Education and Merit", "https://www.mim.gov.it/"),
        Jurisdiction("BR", "Brazil", "Federal, state and municipal education authorities", "https://www.gov.br/mec/"),
        Jurisdiction("MX", "Mexico", "Secretariat of Public Education", "https://www.gob.mx/sep"),
        Jurisdiction("AR", "Argentina", "National and provincial education authorities", "https://www.argentina.gob.ar/educacion"),
        Jurisdiction("CL", "Chile", "Ministry of Education", "https://www.mineduc.cl/"),
        Jurisdiction("CO", "Colombia", "Ministry of National Education", "https://www.mineducacion.gov.co/"),
        Jurisdiction("AE", "United Arab Emirates", "Ministry of Education / emirate authorities", "https://www.moe.gov.ae/"),
        Jurisdiction("SA", "Saudi Arabia", "Ministry of Education", "https://www.moe.gov.sa/"),
        Jurisdiction("IL", "Israel", "Ministry of Education", "https://www.gov.il/en/departments/ministry_of_education/govil-landing-page"),
        Jurisdiction("CH", "Switzerland", "Cantonal education authorities", "https://www.edk.ch/en"),
        Jurisdiction("NL", "Netherlands", "Ministry of Education, Culture and Science", "https://www.government.nl/ministries/ministry-of-education-culture-and-science"),
        Jurisdiction("SE", "Sweden", "Swedish National Agency for Education", "https://www.skolverket.se/"),
        Jurisdiction("NO", "Norway", "Norwegian Directorate for Education and Training", "https://www.udir.no/"),
        Jurisdiction("DK", "Denmark", "Ministry of Children and Education", "https://www.uvm.dk/"),
        Jurisdiction("FI", "Finland", "Finnish National Agency for Education", "https://www.oph.fi/en"),
        Jurisdiction("PL", "Poland", "Ministry of National Education", "https://www.gov.pl/web/education"),
        Jurisdiction("PT", "Portugal", "Ministry of Education, Science and Innovation", "https://www.portugal.gov.pt/en/gc24/ministries/education-science-and-innovation"),
        Jurisdiction("CZ", "Czech Republic", "Ministry of Education, Youth and Sports", "https://msmt.gov.cz/"),
        Jurisdiction("AT", "Austria", "Federal Ministry of Education", "https://www.bmb.gv.at/en.html"),
        Jurisdiction("BE", "Belgium", "Community education authorities", "https://www.belgium.be/en/education"),
        Jurisdiction("GR", "Greece", "Ministry of Education, Religious Affairs and Sports", "https://www.minedu.gov.gr/"),
        Jurisdiction("TR", "Türkiye", "Ministry of National Education", "https://www.meb.gov.tr/"),
        Jurisdiction("MY", "Malaysia", "Ministry of Education", "https://www.moe.gov.my/"),
        Jurisdiction("TH", "Thailand", "Ministry of Education", "https://www.moe.go.th/"),
        Jurisdiction("PH", "Philippines", "Department of Education", "https://www.deped.gov.ph/"),
        Jurisdiction("ID", "Indonesia", "Ministry of Primary and Secondary Education", "https://www.kemdikbud.go.id/"),
        Jurisdiction("VN", "Vietnam", "Ministry of Education and Training", "https://moet.gov.vn/"),
        Jurisdiction("CN", "China", "Ministry of Education", "http://en.moe.gov.cn/"),
        Jurisdiction("PK", "Pakistan", "Federal and provincial education authorities", "https://www.mofept.gov.pk/"),
        Jurisdiction("BD", "Bangladesh", "Ministry of Education", "https://moedu.gov.bd/"),
        Jurisdiction("LK", "Sri Lanka", "Ministry of Education", "https://moe.gov.lk/"),
        Jurisdiction("KE", "Kenya", "Ministry of Education", "https://www.education.go.ke/"),
        Jurisdiction("NG", "Nigeria", "Federal and state education authorities", "https://education.gov.ng/"),
        Jurisdiction("GH", "Ghana", "Ministry of Education", "https://moe.gov.gh/"),
        Jurisdiction("EG", "Egypt", "Ministry of Education and Technical Education", "https://moete.gov.eg/"),
        Jurisdiction("MA", "Morocco", "Ministry of National Education", "https://www.men.gov.ma/"),
        Jurisdiction("UG", "Uganda", "Ministry of Education and Sports", "https://www.education.go.ug/"),
        Jurisdiction("TZ", "Tanzania", "Ministry of Education, Science and Technology", "https://www.moe.go.tz/"),
        Jurisdiction("RW", "Rwanda", "Ministry of Education", "https://www.mineduc.gov.rw/"),
        Jurisdiction("JM", "Jamaica", "Ministry of Education and Youth", "https://moey.gov.jm/"),
        Jurisdiction("TT", "Trinidad and Tobago", "Ministry of Education", "https://www.moe.gov.tt/"),
        Jurisdiction("FJ", "Fiji", "Ministry of Education", "https://www.education.gov.fj/"),
        Jurisdiction("PG", "Papua New Guinea", "Department of Education", "https://www.education.gov.pg/"),
    )

    fun find(idOrCountry: String): Jurisdiction? {
        val key = idOrCountry.trim().lowercase()
        return jurisdictions.firstOrNull {
            it.id.lowercase() == key || it.countryOrTerritory.lowercase() == key
        }
    }
}
