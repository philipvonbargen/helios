/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.helios.master.resources;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import com.spotify.helios.common.descriptors.DockerVersion;
import com.spotify.helios.common.descriptors.HostInfo;
import com.spotify.helios.common.descriptors.HostStatus;
import com.spotify.helios.common.descriptors.Job;
import com.spotify.helios.common.descriptors.JobId;
import com.spotify.helios.common.descriptors.TaskStatus;
import com.spotify.helios.master.MasterModel;
import com.yammer.dropwizard.views.View;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.CharMatcher.WHITESPACE;
import static java.lang.String.format;

public class HostsView extends View {

  private final String ROOT_URL = "/dashboard/hosts";
  private MasterModel model;

  public HostsView(final MasterModel model) {
    super("hosts.ftl");
    this.model = model;
  }

  public String getRootUrl() {
    return ROOT_URL;
  }

  public String getHostsUrl() {
    return ROOT_URL + "/hosts";
  }

  public String getJobsUrl() {
    return ROOT_URL + "/jobs";
  }

  public List<JobTableEntry> getJobs() {
    // TODO (dxia) The below is copied from JobListCommand.java.
    // Refactor into methods in some class?

    final List<JobTableEntry> entries = Lists.newArrayList();
    for (final Map.Entry<JobId, Job> e : sorted(model.getJobs()).entrySet()) {
      final JobId jobId = e.getKey();
      entries.add(new JobTableEntry() {{
        id = jobId.toShortString();
        name = jobId.getName();
        version = jobId.getVersion();
        hosts = model.getJobStatus(e.getKey()).getDeployments().size();
        command = Joiner.on(' ').join(escape(e.getValue().getCommand()));
        env = Joiner.on(", ").withKeyValueSeparator("=").join(e.getValue().getEnv());
      }});
    }
    return entries;
  }

  public List<HostTableEntry> getHosts() {
    final List<String> hosts = Ordering.natural().sortedCopy(model.listHosts());
    final List<HostTableEntry> entries = Lists.newArrayList();
    for (final String host : hosts) {
      final HostStatus hostStatus = model.getHostStatus(host);
      if (hostStatus == null) {
        continue;
      }

      // TODO (dxia) The below is copied from HostListCommand.java.
      // Refactor into methods in some class?
      final HostInfo hostInfo = hostStatus.getHostInfo();
      final String hiMemUsage;
      final String hiCpus;
      final String hiMem;
      final String hiLoadAvg;
      final String hiOsName;
      final String hiOsVersion;
      final String hiDockerVersion;

      if (hostInfo != null) {
        final long free = hostInfo.getMemoryFreeBytes();
        final long total = hostInfo.getMemoryTotalBytes();
        hiMemUsage = format("%.2f", (float) (total - free) / total);
        hiCpus = String.valueOf(hostInfo.getCpus());
        hiMem = hostInfo.getMemoryTotalBytes() / (1024 * 1024 * 1024) + " gb";
        hiLoadAvg = format("%.2f", hostInfo.getLoadAvg());
        hiOsName = hostInfo.getOsName();
        hiOsVersion = hostInfo.getOsVersion();
        final DockerVersion dv = hostInfo.getDockerVersion();
        hiDockerVersion =
            (dv != null) ? format("%s (%s)", dv.getVersion(), dv.getApiVersion()) : "";
      } else {
        hiMemUsage = hiCpus = hiMem = hiLoadAvg = hiOsName = hiOsVersion = hiDockerVersion = "";
      }

      final String hiHeliosVersion;
      if (hostStatus.getAgentInfo() != null) {
        hiHeliosVersion = Optional.fromNullable(hostStatus.getAgentInfo().getVersion()).or("");
      } else {
        hiHeliosVersion = "";
      }
      entries.add(new HostTableEntry(){{
        name = host;
        status = hostStatus.getStatus().toString();
        jobsDeployed = hostStatus.getJobs().size();
        jobsRunning = countRunning(hostStatus.getStatuses());
        cpus = hiCpus;
        mem = hiMem;
        loadAvg = hiLoadAvg;
        memUsage = hiMemUsage;
        osName = hiOsName;
        osVersion = hiOsVersion;
        heliosVersion = hiHeliosVersion;
        dockerVersion = hiDockerVersion;
      }});
    }
    return entries;
  }

  private int countRunning(final Map<JobId, TaskStatus> statuses) {
    int n = 0;
    for (TaskStatus status : statuses.values()) {
      if (status.getState() == TaskStatus.State.RUNNING) {
        n++;
      }
    }
    return n;
  }

  private List<String> escape(final List<String> args) {
    return Lists.transform(args, new Function<String, String>() {
      @Override
      public String apply(final String input) {
        if (WHITESPACE.matchesAnyOf(input)) {
          return '"' + input + '"';
        } else {
          return input;
        }
      }
    });
  }

  private <K extends Comparable<K>, V> Map<K, V> sorted(final Map<K, V> map) {
    final TreeMap<K, V> sorted = Maps.newTreeMap();
    sorted.putAll(map);
    return sorted;
  }

  public static class JobTableEntry {

    public String id;
    public String name;
    public String version;
    public int hosts;
    public String command;
    public String env;

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getVersion() {
      return version;
    }

    public int getHosts() {
      return hosts;
    }

    public String getCommand() {
      return command;
    }

    public String getEnv() {
      return env;
    }
  }

  public class HostTableEntry {
    public String name;
    public String status;
    public int jobsDeployed;
    public int jobsRunning;
    public String cpus;
    public String mem;
    public String loadAvg;
    public String memUsage;
    public String osName;
    public String heliosVersion;
    public String osVersion;
    public String dockerVersion;

    public String getName() {
      return name;
    }

    public String getStatus() {
      return status;
    }

    public String getJobsDeployed() {
      return String.valueOf(jobsDeployed);
    }

    public String getJobsRunning() {
      return String.valueOf(jobsRunning);
    }

    public String getCpus() {
      return cpus;
    }

    public String getMem() {
      return mem;
    }

    public String getLoadAvg() {
      return loadAvg;
    }

    public String getMemUsage() {
      return memUsage;
    }

    public String getOsName() {
      return osName;
    }

    public String getHeliosVersion() {
      return heliosVersion;
    }

    public String getOsVersion() {
      return osVersion;
    }

    public String getDockerVersion() {
      return dockerVersion;
    }
  }
}
