CREATE TABLE `TestContext`
(
    `eid`       bigint       NOT NULL AUTO_INCREMENT,
    `id`        varchar(100) NOT NULL,
    `context`   json         NOT NULL,
    `processed` tinyint(1) NOT NULL,
    `state`     varchar(100) NOT NULL,
    PRIMARY KEY (`eid`),
    UNIQUE INDEX (id, state)
) ENGINE=InnoDB;