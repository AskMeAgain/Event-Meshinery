package io.github.askmeagain.meshinery.connectors.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class InternalWrapper {

  String context;

  String eid;
}
