
# Social Code Badges with Java

Java servlet web application to request data from GitHub and maven central and render SVG badges.
General badge layout is taken from shields.io.

The application is deployed at: x.h7e.eu

TODO 20250202;jw compression support with /xz prefix is broken

## Running

Requires at least Java 11.

Application can be tested locally with: `mvn jetty:run`

Render a badge locally e.g. by accessing: http://localhost:8080/q/github/starGazers/gh-stargazers/cache2k/cache2k

## Badges

### GitHub Star Gazers

Example Url: `https://x.h7e.eu/badges/xz/q/github/starGazers/gh-stargazers/cache2k/cache2k`

![GitHub Stargazers](https://x.h7e.eu/badges/xz/q/github/starGazers/gh-stargazers/cache2k/cache2k)

### Latest Version on Maven Central

Example Url: `https://x.h7e.eu/badges/xz/q/maven/latestVersion/maven-central/org.cache2k/cache2k-api`

![Maven Central](https://x.h7e.eu/badges/xz/q/maven/latestVersion/maven-central/org.cache2k/cache2k-api)

### Free form badge

Example Url: `https://x.h7e.eu/badges/xz/txt/hello/world`

![Hello World!](https://x.h7e.eu/badges/xz/txt/hello/world)

### Free form with different color

Example Url: `https://x.h7e.eu/badges/xz/txt/hello,127/world,a23`

![Hello World!](https://x.h7e.eu/badges/xz/txt/hello,127/world,a23)

