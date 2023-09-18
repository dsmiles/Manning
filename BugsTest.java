package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.*;

public class BugsTest {

    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;

    @BeforeEach
    public void setup() {
        searchResults = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
    }

    @Test
    public void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        final int maximumShowcaseItems = 5;
        final var partnerVendor = makeVendor(Partner);
        var missing = givenAssetInResultsWithVendor(partnerVendor);
        givenAssetInResultsWithVendor(makeVendor(Partner));
        var expected = givenAssetsInResultsWithVendor(maximumShowcaseItems - 1, partnerVendor);

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, missing);
        thenHotspotHasExactly(Showcase, expected);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        var id = Any.string();
        var displayName = Any.string();
        var royaltyRate = Any.anyLong();

        return new AssetVendor(id, displayName, relationshipLevel, royaltyRate);
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        var id = Any.string();
        var title = Any.string();
        var thumbnailURI = Any.URI();
        var previewURI = Any.URI();
        var last30Days = Any.assetPurchaseInfo();
        var last24Hours = Any.assetPurchaseInfo();
        var topics = new ArrayList<AssetTopic>();
        topics.add(Any.anyTopic());
        topics.add(Any.anyTopic());

        var asset = new Asset(id, title, thumbnailURI, previewURI, last30Days, last24Hours, topics, vendor);
        searchResults.addFound(asset);

        return asset;
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