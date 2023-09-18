package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.*;

public class BugsTest {
    final int maximumShowcaseItems = 5;
    private AssetVendor partnerVendor;
    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;

    @BeforeEach
    public void setup() {
        partnerVendor = makeVendor(Partner);
        searchResults = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
    }

    @Test
    public void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        var missing = givenAssetInResultsWithVendor(partnerVendor);
        givenAssetInResultsWithVendor(makeVendor(Partner));
        var expected = givenAssetsInResultsWithVendor(maximumShowcaseItems - 1, partnerVendor);

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, missing);
        thenHotspotHasExactly(Showcase, expected);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel level) {
        return new AssetVendor("anything", "anything", level, 1);
    }


    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        Asset asset = getAsset(vendor);
        searchResults.addFound(asset);
        return asset;
    }

    private Asset getAsset(AssetVendor vendor) {
        return new Asset("anything", "anything", null, null, getPurchaseInfo(), getPurchaseInfo(),
                new ArrayList<>(), vendor);
    }

    private AssetPurchaseInfo getPurchaseInfo() {
        return new AssetPurchaseInfo(0, 0,
                new Money(new BigDecimal("0")),
                new Money(new BigDecimal("0")));
    }

    private ArrayList<Asset> givenAssetsInResultsWithVendor(int count, AssetVendor vendor) {
        var result = new ArrayList<Asset>();
        for (var i = 0; i < count; ++i) {
            result.add(givenAssetInResultsWithVendor(vendor));
        }
        return result;
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
    }

    private void thenHotspotDoesNotHave(HotspotKey key, Asset... forbidden) {
        for (var asset : forbidden) {
            assertFalse(searchResults.getHotspot(key).getMembers().contains(asset));
        }
    }

    private void thenHotspotHasExactly(HotspotKey key, List<Asset> expected) {
        assertArrayEquals(expected.toArray(), searchResults.getHotspot(key).getMembers().toArray());
    }
}
