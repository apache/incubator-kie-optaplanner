<!--
Thank you for submitting this pull request.

Please provide all relevant information as outlined below. Feel free to delete
a section if that type of information is not available.
-->

### JIRA

<!-- Add a JIRA ticket link if it exists. -->
<!-- Example: https://issues.redhat.com/browse/PLANNER-1234 -->

### Referenced pull requests

<!-- Add URLs of all referenced pull requests if they exist. This is only required when making
changes that span multiple kiegroup repositories and depend on each other. -->
<!-- Example:
* https://github.com/kiegroup/droolsjbpm-build-bootstrap/pull/1234
* https://github.com/kiegroup/drools/pull/3000
* https://github.com/kiegroup/optaplanner/pull/899
* etc.
-->

### Checklist
- [ ] Documentation updated if applicable.
- [ ] Upgrade recipe provided if applicable.

<details>
<summary>
How to retest this PR or trigger a specific build:
</summary>

* for a <b>pull request build</b> please add comment: <b>Jenkins retest this</b>
* for a <b>full downstream build</b> please add comment: <b>Jenkins run fdb</b>
* for a <b>compile downstream build</b> please add comment: <b>Jenkins run cdb</b>
* for a <b>full production downstream build</b> please add comment: <b>Jenkins execute product fdb</b>
* for an <b>upstream build</b> please add comment: <b>Jenkins run upstream</b>
* for a <b>Quarkus LTS check</b> please add comment: <b>Jenkins run LTS</b>
* for a <b>Native check</b> please add comment: <b>Jenkins run native</b>
</details>

<details>
<summary>
How to use multijob PR check:
</summary>
<b>To use the multijob PR check, you will need to add the `multijob-pr` label to the PR</b>

The multijob PR check is running different jobs for the current repository and each downstream repository, one after the other (or parallel)
with the following dependency graph:

          optaplanner
              |
            -----
            |    |
          apps   examples

Here are the different commands available to run/rerun multijob jobs:

* <b>Run (or rerun) all tests</b>  
  Please add comment: <b>Jenkins (re)run multijob tests</b> or <b>Jenkins retest this</b>

* <b>Run (or rerun) dependent test(s)</b>  
  Please add comment: <b>Jenkins (re)run multijob [apps|examples] tests</b>

* <b>Run (or rerun) all LTS tests</b>  
  Please add comment: <b>Jenkins (re)run multijob LTS</b> or <b>Jenkins run LTS</b>

* <b>Run (or rerun) LTS dependent test(s)</b>  
  Please add comment: <b>Jenkins (re)run multijob [apps|examples] LTS</b>

* <b>Run (or rerun) all native tests</b>  
  Please add comment: <b>Jenkins (re)run multijob native</b> or <b>Jenkins run native</b>

* <b>Run (or rerun) native dependent test(s)</b>  
  Please add comment: <b>Jenkins (re)run multijob [apps|examples] native</b>

*NOTE: Running a dependent test will run also following dependent projects.*
</details>
