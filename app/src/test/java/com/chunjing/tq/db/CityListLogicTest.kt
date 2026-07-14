package com.chunjing.tq.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class CityListLogicTest {

    @Test
    fun isEquivalentForTabs_sameContent_returnsTrue() {
        val a = listOf(
            CityListLogic.CityTabSnapshot("100000", 0, "1", "2", "定位", "定位区"),
            CityListLogic.CityTabSnapshot("101", 1, "3", "4", "北京", "北京")
        )
        assertTrue(CityListLogic.isEquivalentForTabs(a, a.map { it.copy() }))
    }

    @Test
    fun isEquivalentForTabs_differentOrder_returnsFalse() {
        val old = listOf(
            snap("a", 1),
            snap("b", 2)
        )
        val new = listOf(
            snap("b", 1),
            snap("a", 2)
        )
        assertFalse(CityListLogic.isEquivalentForTabs(old, new))
    }

    @Test
    fun refreshWindowIds_centerWithNeighbors() {
        val ids = listOf("loc", "bj", "sh", "gz")
        assertEquals(setOf("loc", "bj", "sh"), CityListLogic.refreshWindowIds(ids, 1))
        assertEquals(setOf("bj", "sh", "gz"), CityListLogic.refreshWindowIds(ids, 2))
        assertEquals(setOf("loc", "bj"), CityListLogic.refreshWindowIds(ids, 0))
        assertEquals(setOf("sh", "gz"), CityListLogic.refreshWindowIds(ids, 3))
    }

    @Test
    fun refreshWindowIds_empty() {
        assertTrue(CityListLogic.refreshWindowIds(emptyList(), 0).isEmpty())
    }

    @Test
    fun sortOrdersFor_locationPinnedAndOthersIncremental() {
        val orders = CityListLogic.sortOrdersFor(
            listOf(
                "bj" to false,
                "sh" to false,
                "gz" to false
            )
        )
        assertEquals(listOf("bj" to 1, "sh" to 2, "gz" to 3), orders)
    }

    @Test
    fun assignSortOrder_rules() {
        assertEquals(0, CityListLogic.assignSortOrder("100000", true, null, 5))
        assertEquals(3, CityListLogic.assignSortOrder("101", false, 3, 5))
        assertEquals(6, CityListLogic.assignSortOrder("102", false, null, 5))
    }

    private fun snap(id: String, order: Int) =
        CityListLogic.CityTabSnapshot(id, order, "", "", id, id)
}

class CacheCodecTest {

    data class SamplePayload(val name: String, val value: Int) : Serializable

    @Test
    fun encodeDecode_roundTrip() {
        val original = SamplePayload("weather", 42)
        val bytes = CacheCodec.encode(original)
        assertTrue(CacheCodec.isJsonEncoded(bytes))
        val decoded = CacheCodec.decode(bytes) as SamplePayload
        assertEquals(original, decoded)
    }

    @Test
    fun decode_legacyJavaSerialized() {
        val original = SamplePayload("legacy", 7)
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(original) }
        val bytes = baos.toByteArray()
        assertFalse(CacheCodec.isJsonEncoded(bytes))
        val decoded = CacheCodec.decode(bytes) as SamplePayload
        assertEquals(original, decoded)
    }

    @Test
    fun decode_corrupt_returnsNull() {
        assertEquals(null, CacheCodec.decode(byteArrayOf(1, 2, 3, 4)))
    }
}
