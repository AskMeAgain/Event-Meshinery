package io.github.askmeagain.meshinery.connectors.mysql;

import lombok.RequiredArgsConstructor;
import org.jdbi.v3.json.Json;

@RequiredArgsConstructor
public class Wrapper<C> {

  @Json
  private final C data;
  private String eid;
}
