Feature: Cluster orchestration.
  External orchestrator must be able to coordinate role based activities on Jenkins Akka cluster.

  Scenario: A new member joins the cluster with no other instances and becomes active master
    Given there are no existing Jenkins instances on the cluster
    When a new Jenkins instance joins the cluster
    Then I should see the same Jenkins instance become a cluster member
    And I should see the same Jenkins instance become the active master

  Scenario: A new member joins the cluster with other instances and becomes a hot standby instance
    Given there are existing Jenkins instances on the cluster
    And there is an active master on the cluster
    When a new Jenkins instance joins the cluster
    Then I should see the same Jenkins instance become a cluster member
    And I should see the same Jenkins instance become hot standby instance

  Scenario: Failover on active master leaving cluster, another hot standby instance becomes active master
    Given there are existing Jenkins instances on the cluster
    And there is an active master on the cluster
    When active master leaves cluster
    Then I should see one of the other hot standby Jenkins instances become active master

  Scenario: When a new active master assignment is made, proxy must point to new active master
    Given there are existing Jenkins instances on the cluster
    And there is an active master on the cluster
    When active master leaves cluster
    Then I should see new assigned active master to be visible as the proxy resolution

  @todo
  Scenario: When a new Jenkins instance becomes hot standby, all existing jobs should be deleted and all active master jobs should be synced to it
    Given there is an active master on the cluster
    When a new Jenkins instance joins the cluster
    Then I should see the same Jenkins instance become a cluster member
    And I should see the same Jenkins instance becomes a hot standby
    And I should see the same Jenkins instance existing jobs completely deleted
    And I should see the same Jenkins instance has active master's jobs

  @todo
  Scenario: When a new active master is selected, all its existing jobs should be created in hot standby instances
    Given there is an active master on the cluster
    When active master change occurs
    Then I should see all jobs and builds in active master to exist in hot standby instances
