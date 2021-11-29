CREATE TABLE `VotingContext`
(
    `context`   json         NOT NULL,
    `id`        varchar(100) NOT NULL,
    `processed` tinyint(1)   NOT NULL,
    `eid`       bigint       NOT NULL AUTO_INCREMENT,
    `state`     varchar(100) NOT NULL,
    PRIMARY KEY (`eid`),
    UNIQUE KEY(id, state, processed)
) ENGINE=InnoDB;
