package com.moneyapp.parser

object CategoryGuesser {

    private val rules: List<Pair<Regex, String>> = listOf(
        Regex("(스타벅스|투썸|이디야|메가커피|커피|카페)") to "카페",
        Regex("(편의점|CU|GS25|세븐일레븐|이마트24)") to "편의점",
        Regex("(맥도날드|버거킹|롯데리아|KFC|배달|배민|쿠팡이츠|요기요|음식|식당)") to "식비",
        Regex("(이마트|홈플러스|롯데마트|마트)") to "장보기",
        Regex("(GS칼텍스|SK주유|현대오일|주유)") to "교통/주유",
        Regex("(지하철|버스|티머니|택시|카카오T|카카오택시)") to "교통",
        Regex("(CGV|메가박스|롯데시네마|영화|넷플릭스|왓챠|유튜브)") to "문화",
        Regex("(병원|약국|의원|치과)") to "의료",
        Regex("(쿠팡|11번가|G마켓|옥션|네이버|무신사|쇼핑)") to "쇼핑",
        Regex("(통신|SK텔레콤|KT|LGU|요금)") to "통신",
        Regex("(월세|관리비|전기|도시가스|수도)") to "주거"
    )

    fun guess(merchant: String?, defaultCategory: String = "기타"): String {
        if (merchant.isNullOrBlank()) return defaultCategory
        return rules.firstOrNull { it.first.containsMatchIn(merchant) }?.second ?: defaultCategory
    }
}
