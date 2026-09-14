# Education business boundary

`modules/business/education` is a business bounded context layered on top of the reusable
platform. The platform owns execution, model, knowledge, IAM, persistence infrastructure and
generic web/authentication contracts; education owns learning workflows, education nodes, EDU
tables, education permissions/navigation and education-specific web routes.

## Composition rules

- `shiyu-application` contains only the platform baseline. Optional business modules contribute
  schema, seed data and expected tables through `DatabaseBaselineContributor`.
- `shiyu-platform-bootstrap` packages the platform without the education implementation.
- `shiyu-ai-bootstrap` is the education-enabled composition and explicitly depends on
  `shiyu-education-implementation`.
- Platform agent node types are extensible descriptors. Education registers its six node types from
  the education contract rather than adding business values to the platform contract.
- Education web routing and public paths are contributed by education beans. The shared web layer
  does not contain education-specific routes.
- Education permissions and navigation are IAM extensions, not platform seed data. Their seed files
  declare `@schema-extension iam`, so the cross-domain write is explicit and checked as an allowed
  composition hook.

## Acceptance checks

1. Platform `NodeType.values()` contains no `EDUCATION_*` values.
2. A platform-only database baseline has no `EDU_*` tables or education seed resources.
3. An education-enabled baseline still installs the complete education schema and seed data.
4. The platform bootstrap builds without a production dependency on
   `shiyu-education-implementation`.

## Explicitly not exercised

- There is no checked-in pre-migration OpenAPI/schema snapshot or production database copy, so
  compatibility was verified through unchanged controller mappings, education controller tests,
  schema ownership checks and a fresh APP_HOME startup rather than by replaying an old database.
- No external plugin binary fixture is configured in this repository; the module and contract
  boundary checks therefore cover the in-repository SPI consumers only.
