/*
 *  Copyright 1999-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.l4rue.cime.internal.util;

import java.util.*;

/**
 * This class implements a String cache for ByteChunk and CharChunk.
 *
 * @author dingyh
 */
public class StringCache {

    // ------------------------------------------------------- Static Variables

    /**
     * Enabled ?
     */
    protected static boolean byteEnabled =
            ("true".equals(System.getProperty("tomcat.util.buf.StringCache.byte.enabled", "false")));

    protected static boolean charEnabled =
            ("true".equals(System.getProperty("tomcat.util.buf.StringCache.char.enabled", "false")));

    protected static int trainThreshold =
            Integer.parseInt(System.getProperty("tomcat.util.buf.StringCache.trainThreshold", "20000"));

    protected static int cacheSize =
            Integer.parseInt(System.getProperty("tomcat.util.buf.StringCache.cacheSize", "200"));

    /**
     * Statistics hash map for byte chunk.
     */
    protected static HashMap<ByteEntry, int[]> bcStats = new HashMap<ByteEntry, int[]>(cacheSize);

    /**
     * toString count for byte chunk.
     */
    protected static int bcCount = 0;

    /**
     * Cache for byte chunk.
     */
    protected static ByteEntry[] bcCache = null;

    /**
     * Statistics hash map for char chunk.
     */
    protected static HashMap<CharEntry, int[]> ccStats = new HashMap<CharEntry, int[]>(cacheSize);

    /**
     * toString count for char chunk.
     */
    protected static int ccCount = 0;

    /**
     * Cache for char chunk.
     */
    protected static CharEntry[] ccCache = null;

    /**
     * Access count.
     */
    protected static int accessCount = 0;

    /**
     * Hit count.
     */
    protected static int hitCount = 0;

    // ------------------------------------------------------------ Properties

    public static String toString(CharChunk cc) {

        // If the cache is null, then either caching is disabled, or we're
        // still training
        if (ccCache == null) {
            String value = cc.toStringInternal();
            if (charEnabled) {
                // If training, everything is synced
                synchronized (ccStats) {
                    // If the cache has been generated on a previous invocation
                    // while waiting fot the lock, just return the toString value
                    // we just calculated
                    if (ccCache != null) {
                        return value;
                    }
                    // Two cases: either we just exceeded the train count, in which
                    // case the cache must be created, or we just update the count for
                    // the string
                    if (ccCount > trainThreshold) {
                        // Sort the entries according to occurrence
                        TreeMap<Integer, List<CharEntry>> tempMap = new TreeMap<Integer, List<CharEntry>>();
                        Iterator<CharEntry> entries = ccStats.keySet().iterator();
                        while (entries.hasNext()) {
                            CharEntry entry = entries.next();
                            int[] countA = ccStats.get(entry);
                            Integer count = Integer.valueOf(countA[0]);
                            // Add to the list for that count
                            List<CharEntry> list = tempMap.get(count);
                            if (list == null) {
                                // Create list
                                list = new ArrayList<CharEntry>();
                                tempMap.put(count, list);
                            }
                            list.add(entry);
                        }
                        // Allocate array of the right size
                        int size = ccStats.size();
                        if (size > cacheSize) {
                            size = cacheSize;
                        }
                        CharEntry[] tempccCache = new CharEntry[size];
                        // Fill it up using an alphabetical order
                        // and a dumb insert sort
                        CharChunk tempChunk = new CharChunk();
                        int n = 0;
                        while (n < size) {
                            Integer key = tempMap.lastKey();
                            List<CharEntry> list = tempMap.get(key);
                            for (int i = 0; i < list.size() && n < size; i++) {
                                CharEntry entry = list.get(i);
                                tempChunk.setChars(entry.name, 0, entry.name.length);
                                int insertPos = findClosest(tempChunk, tempccCache, n);
                                if (insertPos == n) {
                                    tempccCache[n + 1] = entry;
                                } else {
                                    System.arraycopy(tempccCache, insertPos + 1, tempccCache,
                                            insertPos + 2, n - insertPos - 1);
                                    tempccCache[insertPos + 1] = entry;
                                }
                                n++;
                            }
                            tempMap.remove(key);
                        }
                        ccCount = 0;
                        ccStats.clear();
                        ccCache = tempccCache;
                    } else {
                        ccCount++;
                        // Allocate new CharEntry for the lookup
                        CharEntry entry = new CharEntry();
                        entry.value = value;
                        int[] count = ccStats.get(entry);
                        if (count == null) {
                            int end = cc.getEnd();
                            int start = cc.getStart();
                            // Create char array and copy chars
                            entry.name = new char[cc.getLength()];
                            System.arraycopy(cc.getBuffer(), start, entry.name, 0, end - start);
                            // Initialize occurrence count to one
                            count = new int[1];
                            count[0] = 1;
                            // Set in the stats hash map
                            ccStats.put(entry, count);
                        } else {
                            count[0] = count[0] + 1;
                        }
                    }
                }
            }
            return value;
        } else {
            accessCount++;
            // Find the corresponding String
            String result = find(cc);
            if (result == null) {
                return cc.toStringInternal();
            }
            // Note: We don't care about safety for the stats
            hitCount++;
            return result;
        }
    }

    /**
     * Compare given char chunk with char array.
     * Return -1, 0 or +1 if inferior, equal, or superior to the String.
     */
    protected static final int compare(CharChunk name, char[] compareTo) {
        int result = 0;

        char[] c = name.getBuffer();
        int start = name.getStart();
        int end = name.getEnd();
        int len = compareTo.length;

        if ((end - start) < len) {
            len = end - start;
        }
        for (int i = 0; (i < len) && (result == 0); i++) {
            if (c[i + start] > compareTo[i]) {
                result = 1;
            } else if (c[i + start] < compareTo[i]) {
                result = -1;
            }
        }
        if (result == 0) {
            if (compareTo.length > (end - start)) {
                result = -1;
            } else if (compareTo.length < (end - start)) {
                result = 1;
            }
        }
        return result;
    }

    /**
     * Find an entry given its name in the cache and return the associated String.
     */
    protected static final String find(CharChunk name) {
        int pos = findClosest(name, ccCache, ccCache.length);
        if ((pos < 0) || (compare(name, ccCache[pos].name) != 0)) {
            return null;
        } else {
            return ccCache[pos].value;
        }
    }

    /**
     * Find an entry given its name in a sorted array of map elements.
     * This will return the index for the closest inferior or equal item in the
     * given array.
     */
    protected static final int findClosest(CharChunk name, CharEntry[] array, int len) {

        int a = 0;
        int b = len - 1;

        // Special cases: -1 and 0
        if (b == -1) {
            return -1;
        }

        if (compare(name, array[0].name) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }

        int i = 0;
        while (true) {
            i = (b + a) / 2;
            int result = compare(name, array[i].name);
            if (result == 1) {
                a = i;
            } else if (result == 0) {
                return i;
            } else {
                b = i;
            }
            if ((b - a) == 1) {
                int result2 = compare(name, array[b].name);
                if (result2 < 0) {
                    return a;
                } else {
                    return b;
                }
            }
        }
    }

    /**
     * @return Returns the cacheSize.
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * @param cacheSize The cacheSize to set.
     */
    public void setCacheSize(int cacheSize) {
        StringCache.cacheSize = cacheSize;
    }

    /**
     * @return Returns the enabled.
     */
    public boolean getByteEnabled() {
        return byteEnabled;
    }

    /**
     * @param byteEnabled The enabled to set.
     */
    public void setByteEnabled(boolean byteEnabled) {
        StringCache.byteEnabled = byteEnabled;
    }

    /**
     * @return Returns the enabled.
     */
    public boolean getCharEnabled() {
        return charEnabled;
    }

    /**
     * @param charEnabled The enabled to set.
     */
    public void setCharEnabled(boolean charEnabled) {
        StringCache.charEnabled = charEnabled;
    }

    // -------------------------------------------------- Public Static Methods

    /**
     * @return Returns the trainThreshold.
     */
    public int getTrainThreshold() {
        return trainThreshold;
    }

    /**
     * @param trainThreshold The trainThreshold to set.
     */
    public void setTrainThreshold(int trainThreshold) {
        StringCache.trainThreshold = trainThreshold;
    }

    // ----------------------------------------------------- Protected Methods

    /**
     * @return Returns the accessCount.
     */
    public int getAccessCount() {
        return accessCount;
    }

    /**
     * @return Returns the hitCount.
     */
    public int getHitCount() {
        return hitCount;
    }

    public void reset() {
        hitCount = 0;
        accessCount = 0;
        synchronized (bcStats) {
            bcCache = null;
            bcCount = 0;
        }
        synchronized (ccStats) {
            ccCache = null;
            ccCount = 0;
        }
    }

    // -------------------------------------------------- ByteEntry Inner Class

    public static class ByteEntry {

        public byte[] name = null;
        public String enc = null;
        public String value = null;

        public int hashCode() {
            return value.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof ByteEntry) {
                return value.equals(((ByteEntry) obj).value);
            }
            return false;
        }

        public String toString() {
            return value;
        }
    }

    // -------------------------------------------------- CharEntry Inner Class

    public static class CharEntry {

        public char[] name = null;
        public String value = null;

        public int hashCode() {
            return value.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof CharEntry) {
                return value.equals(((CharEntry) obj).value);
            }
            return false;
        }

        public String toString() {
            return value;
        }
    }
}
