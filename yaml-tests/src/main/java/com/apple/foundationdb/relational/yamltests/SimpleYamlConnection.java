/*
 * SimpleYamlConnection.java
 *
 * This source file is part of the FoundationDB open source project
 *
 * Copyright 2015-2025 Apple Inc. and the FoundationDB project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apple.foundationdb.relational.yamltests;

import com.apple.foundationdb.relational.api.Options;
import com.apple.foundationdb.relational.api.RelationalConnection;
import com.apple.foundationdb.relational.api.RelationalPreparedStatement;
import com.apple.foundationdb.relational.api.RelationalStatement;
import com.apple.foundationdb.relational.api.metrics.MetricCollector;
import com.apple.foundationdb.relational.recordlayer.EmbeddedRelationalConnection;
import com.apple.foundationdb.relational.yamltests.server.SemanticVersion;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Assumptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * A simple version of {@link YamlConnection} for interacting with a single {@link RelationalConnection}.
 */
public class SimpleYamlConnection implements YamlConnection {
    @Nonnull
    private final RelationalConnection underlying;
    @Nonnull
    private final List<SemanticVersion> versions;
    @Nonnull
    private final String connectionLabel;

    public SimpleYamlConnection(@Nonnull Connection connection, @Nonnull SemanticVersion version) throws SQLException {
        this(connection, version, version.toString());
    }

    public SimpleYamlConnection(@Nonnull Connection connection, @Nonnull SemanticVersion version, @Nonnull String connectionLabel) throws SQLException {
        underlying = connection.unwrap(RelationalConnection.class);
        this.versions = List.of(version);
        this.connectionLabel = connectionLabel;
    }

    @Nonnull
    protected RelationalConnection getUnderlying() {
        return underlying;
    }

    @Override
    public void close() throws SQLException {
        underlying.close();
    }

    @Override
    public void setConnectionOptions(@Nonnull final Options connectionOptions) throws SQLException {
        if (!Iterables.isEmpty(connectionOptions.entries())) {
            Assumptions.abort("only embedded connections support the setting of connection options");
        }
    }

    @Override
    public boolean supportsMetricCollector() {
        return underlying instanceof EmbeddedRelationalConnection;
    }

    @Override
    public RelationalStatement createStatement() throws SQLException {
        return underlying.createStatement();
    }

    @Override
    public RelationalPreparedStatement prepareStatement(final String sql) throws SQLException {
        return underlying.prepareStatement(sql);
    }

    @Nullable
    @Override
    public MetricCollector getMetricCollector() {
        return ((EmbeddedRelationalConnection) underlying).getMetricCollector();
    }

    @Nullable
    @Override
    public EmbeddedRelationalConnection tryGetEmbedded() {
        if (underlying instanceof EmbeddedRelationalConnection) {
            return (EmbeddedRelationalConnection)underlying;
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public List<SemanticVersion> getVersions() {
        return versions;
    }

    @Nonnull
    @Override
    public SemanticVersion getInitialVersion() {
        return versions.get(0);
    }

    @Override
    public String toString() {
        return connectionLabel;
    }
}
