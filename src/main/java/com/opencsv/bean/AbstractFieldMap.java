/*
 * Copyright 2018 Andrew Rucker Jones.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opencsv.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang3.ObjectUtils;

/**
 * A base class to collect all generalized components of a {@link FieldMap}.
 * May be used by all as a base class for their own implementations of
 * {@link FieldMap}.
 * 
 * @param <I> The initializer type used to build the many-to-one mapping
 * @param <K> Type of the field identifier (key)
 * @param <C> Type of the ComplexFieldMapEntry used
 * @param <T> Type of the bean being converted
 * 
 * @author Andrew Rucker Jones
 * @since 4.2
 */
abstract public class AbstractFieldMap<I, K extends Comparable<K>, C extends ComplexFieldMapEntry<I, K, T>, T>
        implements FieldMap<I, K, C, T> {
    
    /** The locale for error messages. */
    protected Locale errorLocale;
    
    /**
     * A map for all simple, that is one-to-one, mappings represented in this
     * {@link FieldMap}.
     */
    protected final SortedMap<K, BeanField<T>> simpleMap = new TreeMap<>();
    
    /**
     * A list of entries representing all complex, that is many-to-one, mappings
     * represented in this {@link FieldMap}.
     */
    protected final List<C> complexMapList = new ArrayList<>();
    
    /**
     * Initializes this {@link FieldMap}.
     * 
     * @param errorLocale The locale to be used for error messages
     */
    public AbstractFieldMap(final Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
    }
    
    @Override
    public BeanField<T> get(final K key) {
        BeanField<T> f = simpleMap.get(key);
        if(f == null) {
            f = complexMapList.stream()
                    .filter(r -> r.contains(key))
                    .map(ComplexFieldMapEntry::getBeanField)
                    .findAny().orElse(null);
            // Would love to do .orElse(simpleMap.get(key)) and shorten this,
            // but that changes the order of precedence.
        }
        return f;
    }
    
    @Override
    public BeanField<T> put(final K key, final BeanField<T> value) {
        return simpleMap.put(key, value);
    }
    
    @Override
    public Collection<BeanField<T>> values() {
        final List<BeanField<T>> l = new ArrayList<>(simpleMap.size() + complexMapList.size());
        l.addAll(simpleMap.values());
        complexMapList.forEach(r -> l.add(r.getBeanField()));
        return l;
    }
    
    @Override
    public void setErrorLocale(final Locale errorLocale) {
        this.errorLocale = ObjectUtils.defaultIfNull(errorLocale, Locale.getDefault());
        complexMapList.forEach(e -> e.setErrorLocale(this.errorLocale));
    }
}
