/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.api;

@SuppressWarnings("rawtypes")
public record RegistrationInfo(SupportedBlockEntityTypes blockEntityType,
                               AltRendererProvider rendererProvider) {
}
