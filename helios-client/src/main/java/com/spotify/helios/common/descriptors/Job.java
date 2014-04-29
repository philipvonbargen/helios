/**
 * Copyright (C) 2013 Spotify AB
 */

package com.spotify.helios.common.descriptors;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.helios.common.Json;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.spotify.helios.common.Hash.sha1digest;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Job extends Descriptor implements Comparable<Job> {

  public static final Map<String, String> EMPTY_ENV = emptyMap();
  public static final Map<String, PortMapping> EMPTY_PORTS = emptyMap();
  public static final List<String> EMPTY_COMMAND = emptyList();
  public static final Map<ServiceEndpoint, ServicePorts> EMPTY_REGISTRATION = emptyMap();

  private final JobId id;
  private final String image;
  private final List<String> command;
  private final Map<String, String> env;
  private final Map<String, PortMapping> ports;
  private final Map<ServiceEndpoint, ServicePorts> registration;

  public Job(@JsonProperty("id") final JobId id,
             @JsonProperty("image") final String image,
             @JsonProperty("command") final @Nullable List<String> command,
             @JsonProperty("env") final @Nullable Map<String, String> env,
             @JsonProperty("ports") final @Nullable Map<String, PortMapping> ports,
             @JsonProperty("registration")
             final @Nullable Map<ServiceEndpoint, ServicePorts> registration) {
    this.id = checkNotNull(id, "id");
    this.image = checkNotNull(image, "image");

    // Optional
    this.command = Optional.fromNullable(command).or(EMPTY_COMMAND);
    this.env = Optional.fromNullable(env).or(EMPTY_ENV);
    this.ports = Optional.fromNullable(ports).or(EMPTY_PORTS);
    this.registration = registration;
  }

  private Job(final JobId id, final Builder.Parameters p) {
    this.id = checkNotNull(id, "id");
    this.image = checkNotNull(p.image, "image");
    this.command = ImmutableList.copyOf(checkNotNull(p.command, "command"));
    this.env = ImmutableMap.copyOf(checkNotNull(p.env, "env"));
    this.ports = ImmutableMap.copyOf(checkNotNull(p.ports, "ports"));
    this.registration = ImmutableMap.copyOf(checkNotNull(p.registration, "registration"));
  }

  public JobId getId() {
    return id;
  }

  public String getImage() {
    return image;
  }

  public List<String> getCommand() {
    return command;
  }

  public Map<String, String> getEnv() {
    return env;
  }

  public Map<String, PortMapping> getPorts() {
    return ports;
  }

  public Map<ServiceEndpoint, ServicePorts> getRegistration() {
    return registration;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public int compareTo(final Job o) {
    return id.compareTo(o.getId());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Job job = (Job) o;

    if (command != null ? !command.equals(job.command) : job.command != null) {
      return false;
    }
    if (env != null ? !env.equals(job.env) : job.env != null) {
      return false;
    }
    if (id != null ? !id.equals(job.id) : job.id != null) {
      return false;
    }
    if (image != null ? !image.equals(job.image) : job.image != null) {
      return false;
    }
    if (ports != null ? !ports.equals(job.ports) : job.ports != null) {
      return false;
    }
    if (registration != null ? !registration.equals(job.registration)
                             : job.registration != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (image != null ? image.hashCode() : 0);
    result = 31 * result + (command != null ? command.hashCode() : 0);
    result = 31 * result + (env != null ? env.hashCode() : 0);
    result = 31 * result + (ports != null ? ports.hashCode() : 0);
    result = 31 * result + (registration != null ? registration.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", id)
        .add("image", image)
        .add("command", command)
        .add("env", env)
        .add("ports", ports)
        .add("registration", registration)
        .toString();
  }

  public Builder toBuilder() {
    return newBuilder()
        .setName(id.getName())
        .setVersion(id.getVersion())
        .setImage(image)
        .setCommand(command)
        .setEnv(env)
        .setPorts(ports)
        .setRegistration(registration);
  }

  public static class Builder {

    private String hash;

    public static class Parameters {

      // Note: Changing the fields of this class will affect the id's of all jobs as it is based
      //       on the sha1 json hash of instances of Parameters.

      public String name;
      public String version;
      public String image;
      public List<String> command = EMPTY_COMMAND;
      public Map<String, String> env = Maps.newHashMap(EMPTY_ENV);
      public Map<String, PortMapping> ports = Maps.newHashMap(EMPTY_PORTS);
      public Map<ServiceEndpoint, ServicePorts> registration = Maps.newHashMap(EMPTY_REGISTRATION);
    }

    final Parameters p = new Parameters();

    public Builder setHash(final String hash) {
      this.hash = hash;
      return this;
    }

    public Builder setName(final String name) {
      p.name = name;
      return this;
    }

    public Builder setVersion(final String version) {
      p.version = version;
      return this;
    }

    public Builder setImage(final String image) {
      p.image = image;
      return this;
    }

    public Builder setCommand(final List<String> command) {
      p.command = ImmutableList.copyOf(command);
      return this;
    }

    public Builder setEnv(final Map<String, String> env) {
      p.env = Maps.newHashMap(env);
      return this;
    }

    public Builder addEnv(String key, String value) {
      p.env.put(key, value);
      return this;
    }

    public Builder setPorts(final Map<String, PortMapping> ports) {
      p.ports = Maps.newHashMap(ports);
      return this;
    }

    public Builder addPort(String name, PortMapping port) {
      p.ports.put(name, port);
      return this;
    }

    public Builder setRegistration(final Map<ServiceEndpoint, ServicePorts> registration) {
      p.registration = Maps.newHashMap(registration);
      return this;
    }

    public Builder addRegistration(ServiceEndpoint endpoint, ServicePorts ports) {
      p.registration.put(endpoint, ports);
      return this;
    }

    public String getName() {
      return p.name;
    }

    public String getVersion() {
      return p.version;
    }

    public String getImage() {
      return p.image;
    }

    public List<String> getCommand() {
      return p.command;
    }

    public Map<String, String> getEnv() {
      return Collections.unmodifiableMap(p.env);
    }

    public Map<String, PortMapping> getPorts() {
      return Collections.unmodifiableMap(p.ports);
    }

    public Map<ServiceEndpoint, ServicePorts> getRegistration() {
      return Collections.unmodifiableMap(p.registration);
    }

    public Job build() {
      final String configHash;
      try {
        configHash = hex(Json.sha1digest(p));
      } catch (IOException e) {
        throw propagate(e);
      }

      final String input = String.format("%s:%s:%s", p.name, p.version, configHash);
      final String hash = hex(sha1digest(input.getBytes(UTF_8)));

      if (this.hash != null) {
        checkArgument(this.hash.equals(hash));
      }

      final JobId id = new JobId(p.name, p.version, hash);

      return new Job(id, p);
    }

    private String hex(final byte[] bytes) {
      return BaseEncoding.base16().lowerCase().encode(bytes);
    }
  }
}
