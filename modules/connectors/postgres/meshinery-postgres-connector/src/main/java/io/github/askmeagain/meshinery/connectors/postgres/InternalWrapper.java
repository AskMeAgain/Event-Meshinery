package io.github.askmeagain.meshinery.connectors.postgres;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalWrapper {

  String context;

  String eid;
}
