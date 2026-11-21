package com.example.platform.ads

/**
 * Pure festival-card Native Ad placement algorithm.
 *
 * Placement rule:
 * - Insert a Native Ad slot after festival card N where N = 1, 3, 5, 7, 9, ...
 *   (i.e. after card 1, then after every two festival cards).
 * - Counts ONLY actual festival cards; never counts section headings, dates,
 *   month labels, dividers, banners, or other UI elements.
 * - Restarts the counting sequence independently for each distinct [feedKey]
 *   (e.g. Home upcoming feed vs. each Festivals category tab vs. search results).
 */
sealed class FestivalFeedEntry<out T> {
    data class SectionHeader(
        val headerKey: String,
        val title: String,
        val subtitle: String? = null
    ) : FestivalFeedEntry<Nothing>()

    data class FestivalItem<T>(
        val item: T,
        val cardNumber: Int,
        val itemKey: String
    ) : FestivalFeedEntry<T>()

    data class NativeAdSlot(
        val feedKey: String,
        val slotNumber: Int,
        val afterCardNumber: Int,
        val itemKey: String
    ) : FestivalFeedEntry<Nothing>() {
        val registrySlotKey: String
            get() = NativeAdSlotRegistry.slotKey(feedKey, slotNumber)
    }
}

object FestivalFeedAdPlacer {

    /**
     * Returns true iff a Native Ad slot should be placed immediately after
     * the 1-based [oneBasedCardNumber] festival card (1, 3, 5, 7, 9, ...).
     */
    fun shouldInsertAdAfterCard(oneBasedCardNumber: Int): Boolean {
        return oneBasedCardNumber >= 1 && oneBasedCardNumber % 2 == 1
    }

    /**
     * Interleaves a flat list of festival items with Native Ad slots using the
     * 1, 3, 5, 7, ... cadence starting independently at card 1 for [feedKey].
     */
    fun <T> interleaveFlat(
        feedKey: String,
        items: List<T>,
        itemKeyProvider: (T) -> String
    ): List<FestivalFeedEntry<T>> {
        if (items.isEmpty()) return emptyList()

        val result = ArrayList<FestivalFeedEntry<T>>(items.size + (items.size + 1) / 2)
        var cardNumber = 0
        var slotNumber = 0

        for (item in items) {
            cardNumber += 1
            result.add(
                FestivalFeedEntry.FestivalItem(
                    item = item,
                    cardNumber = cardNumber,
                    itemKey = "${feedKey}_card_${itemKeyProvider(item)}"
                )
            )
            if (shouldInsertAdAfterCard(cardNumber)) {
                slotNumber += 1
                result.add(
                    FestivalFeedEntry.NativeAdSlot(
                        feedKey = feedKey,
                        slotNumber = slotNumber,
                        afterCardNumber = cardNumber,
                        itemKey = "${feedKey}_ad_slot_$slotNumber"
                    )
                )
            }
        }
        return result
    }

    /**
     * Interleaves grouped festival lists (e.g. grouped by month) while counting ONLY actual
     * festival cards (section headers never increment the card counter) and starting the
     * counting sequence independently at card 1 for each [feedKey] (e.g. each category tab).
     */
    fun <G, T> interleaveGrouped(
        feedKey: String,
        groups: List<G>,
        headerKeyProvider: (G) -> String,
        headerTitleProvider: (G) -> String,
        itemsProvider: (G) -> List<T>,
        itemKeyProvider: (G, T) -> String
    ): List<FestivalFeedEntry<T>> {
        if (groups.isEmpty()) return emptyList()

        val result = mutableListOf<FestivalFeedEntry<T>>()
        var cardNumber = 0
        var slotNumber = 0

        for (group in groups) {
            val groupItems = itemsProvider(group)
            if (groupItems.isEmpty()) continue

            result.add(
                FestivalFeedEntry.SectionHeader(
                    headerKey = "${feedKey}_header_${headerKeyProvider(group)}",
                    title = headerTitleProvider(group)
                )
            )

            for (item in groupItems) {
                cardNumber += 1
                result.add(
                    FestivalFeedEntry.FestivalItem(
                        item = item,
                        cardNumber = cardNumber,
                        itemKey = "${feedKey}_card_${itemKeyProvider(group, item)}"
                    )
                )
                if (shouldInsertAdAfterCard(cardNumber)) {
                    slotNumber += 1
                    result.add(
                        FestivalFeedEntry.NativeAdSlot(
                            feedKey = feedKey,
                            slotNumber = slotNumber,
                            afterCardNumber = cardNumber,
                            itemKey = "${feedKey}_ad_slot_$slotNumber"
                        )
                    )
                }
            }
        }
        return result
    }
}
