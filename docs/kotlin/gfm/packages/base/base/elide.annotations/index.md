//[base](../../index.md)/[elide.annotations](index.md)

# Package elide.annotations

## Types

| Name | Summary |
|---|---|
| [API](-a-p-i/index.md) | [common]<br>@[Target](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-target/index.html)(allowedTargets = [[AnnotationTarget.TYPE](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-annotation-target/-t-y-p-e/index.html)])<br>annotation class [API](-a-p-i/index.md)<br>Marks an application-level class as an API interface, which defines the abstract surface of a single unit of business logic; combined with [Logic](-logic/index.md), classes annotated with `API` constitute a set of interface and implementation pairs. |
| [Endpoint](-endpoint/index.md) | [jvm]<br>@Singleton<br>@[Target](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-target/index.html)(allowedTargets = [[AnnotationTarget.CLASS](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-annotation-target/-c-l-a-s-s/index.html)])<br>annotation class [Endpoint](-endpoint/index.md)<br>Marks a class as an API endpoint, which enables functionality for type conversion between elide.model.WireMessage types and Micronaut requests / responses. |
| [Generated](-generated/index.md) | [common]<br>@[Target](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-target/index.html)(allowedTargets = [[AnnotationTarget.TYPE](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-annotation-target/-t-y-p-e/index.html), [AnnotationTarget.FUNCTION](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-annotation-target/-f-u-n-c-t-i-o-n/index.html)])<br>annotation class [Generated](-generated/index.md)<br>Marks a given Java or Kotlin class as &quot;Generated,&quot; which excuses it from coverage requirements and other tooling strictness; should be used sparingly. |
| [Logic](-logic/index.md) | [common]<br>@[Target](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-target/index.html)(allowedTargets = [[AnnotationTarget.TYPE](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.annotation/-annotation-target/-t-y-p-e/index.html)])<br>annotation class [Logic](-logic/index.md)<br>Marks an application class as &quot;business logic,&quot; which automatically makes it eligible for dependency injection, autowired logging, and other framework features. |
