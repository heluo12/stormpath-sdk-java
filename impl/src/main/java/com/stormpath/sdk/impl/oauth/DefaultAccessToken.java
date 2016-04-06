/*
* Copyright 2015 Stormpath, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.stormpath.sdk.impl.oauth;

import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.oauth.AccessToken;

import java.util.Map;

/**
 * @since 1.0.RC7
 */
public class DefaultAccessToken extends AbstractBaseOauth2Token implements AccessToken {

    public DefaultAccessToken(InternalDataStore dataStore, Map<String, Object> properties) {
        super(dataStore, properties);
        ensureAccessToken();
    }

    /**
     * This method will validate that the received jwt corresponds to an `access_token` (as opposed to a
     * `refresh_token`). If that is not the case then this operation will throw a JwtException. It is called
     * from the constructor, so an AccessToken cannot be instantiated without verifying the jwt is a proper
     * `access_token`
     * 
     * @since 1.0.RC8.3
     */
    private void ensureAccessToken() {
            String href = getStringProperty(HREF_PROP_NAME);
            if (href != null) {
                Assert.isTrue(href.contains("/accessTokens/"), "href does not belong to an access token.");
        }
    }
}
