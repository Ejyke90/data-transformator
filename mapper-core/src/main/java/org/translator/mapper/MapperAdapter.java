package org.translator.mapper;

/**
 * Narrow functional adapter that wraps a concrete mapping implementation.
 * Implementations should be Spring components so the registry can discover
 * them.
 */
public interface MapperAdapter {
    /**
     * Return true if this adapter supports mapping from the given source to the
     * given target.
     * The implementation can match by namespace, message name or normalized
     * identifier.
     */
    boolean supports(String sourceType, String targetType);

    /**
     * Map the given source XML to the target representation and return marshalled
     * XML.
     */
    String map(String sourceXml) throws Exception;
}
