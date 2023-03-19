# BrAPI Validator (BRAVA)

The BrAPI Validator (BRAVA) runs validation tests for BrAPI compliance.

Here you find the following modules for the application

* [api](api/README.md) - The spring API, exposes the validation software in the code module via rest services
* buildSrc - A gradle plugin for contolling the build process of the modules
* [cli](cli/README.md) - The command line version that makes use of the core module
* [core](core/README.md) - The core validation code and util classes
* [database](database/README.md) - Adds support for persisting validation reports in a database and scheduling
  regular validation of endpoints.
* gradle - The gradle wrapper for building, installing etc the modules
* [web](web/README.md) - A simple web application (an alterative to the Angular BRAVA app) that makes use of the spring app using thymeleaf templates
