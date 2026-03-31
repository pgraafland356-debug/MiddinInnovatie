package com.middin.innovatie.app.data.rss

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InnovationNewsKeywordFilterTest {

    @Test
    fun matchesInnovationTopics() {
        assertTrue(
            InnovationNewsKeywordFilter.matchesTitleAndSummary(
                "Ziekenhuis start pilot met telemonitoring",
                "Patiënten met hartfalen gebruiken een app en sensor thuis.",
            ),
        )
        assertTrue(
            InnovationNewsKeywordFilter.matchesTitleAndSummary(
                "Nieuwe richtlijn voor medische hulpmiddelen",
                "Toezicht en registratie.",
            ),
        )
    }

    @Test
    fun rejectsPureReimbursementStoryWithoutTechTerms() {
        assertFalse(
            InnovationNewsKeywordFilter.matchesTitleAndSummary(
                "Hartrevalidatie niet meer vergoed bij pijn op de borst",
                "Nieuwe vergoedingsregels per 1 april.",
            ),
        )
    }

    @Test
    fun districtDoesNotTriggerIctFalsePositive() {
        assertFalse(
            InnovationNewsKeywordFilter.matchesTitleAndSummary(
                "Wijkverpleging in het district",
                "Samenwerking tussen teams in de regio.",
            ),
        )
    }

    @Test
    fun rejectsCareStoryWithoutInnovationSignal() {
        assertFalse(
            InnovationNewsKeywordFilter.matchesTitleAndSummary(
                "Nurses plan one-day strike over pay",
                "Hospitals may reschedule appointments.",
            ),
        )
    }

    @Test
    fun rejectsTechStoryWithoutHealthContext() {
        assertFalse(
            InnovationNewsKeywordFilter.matchesTitleAndSummary(
                "Retailers move inventory systems to the cloud",
                "New SaaS platform cuts logistics cost.",
            ),
        )
    }

    @Test
    fun acceptsAiPoweredHealthHeadline() {
        assertTrue(
            InnovationNewsKeywordFilter.matchesTitleAndSummary(
                "AI-powered tool flags sepsis in ER patients",
                "Hospital pilots software in three emergency departments.",
            ),
        )
    }
}
