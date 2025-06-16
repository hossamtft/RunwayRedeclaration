# Runway Redeclaration Tool (COMP2211 Group Project)

This repository contains the full coursework for the **Runway Redeclaration Tool**—a group project developed for COMP2211: Software Engineering at the University of Southampton.

---

## Overview

The Runway Redeclaration Tool is a Java-based desktop application that helps airport personnel rapidly recalculate and visualize runway declared distances when obstacles are present. It implements official Civil Aviation Authority (CAA) rules and supports multiple user roles (Admin, ATC, Ground Crew).

---

## Features

- **User Authentication & Roles:** Admin, ATC, and ground crew, each with tailored access.
- **Airport and Runway Management:** Add airports, manage runways, and visualize runway layouts.
- **Obstacle Management:** Add obstacles and see recalculated TORA, TODA, ASDA, and LDA.
- **Visualization:** Interactive top-down and side-on runway views with obstacle placement.
- **Export & Import:** Export data and visualizations to XML and PDF, import previous scenarios.
- **Error & Calculation Logs:** Automatic logging for transparency and audit.
- **Comprehensive Help & User Guide:** In-app help and full user documentation.
- **Agile Process:** Developed with iterative increments, full documentation of process and testing.

---

## Documentation

All major project documents are available in the [`docs/`](docs/) folder:
- **Project definition.pdf:** Project background and requirements
- **Envisioning.pdf:** Initial envisioning and goals
- **Increment 1/2/3.pdf:** Agile sprint documentation, design artifacts, UML, and retrospectives
- **Final Report.pdf:** Summary of the completed project and outcomes
- **User Guide.pdf:** Full usage instructions, features, and FAQ

---

## Folder Structure

```
src/                        # Source code
docs/                       # Reports and documentation
    Envisioning.pdf
    Final Report.pdf
    Increment 1.pdf
    Increment 2.pdf
    Increment 3.pdf
    Project definition.pdf
    User Guide.pdf
runwayredeclaration.sqlite  # Application database (auto-generated)
.gitignore
pom.xml 
README.md
```

---

## Credits

Coursework specification © University of Southampton, Electronics & Computer Science.

---
