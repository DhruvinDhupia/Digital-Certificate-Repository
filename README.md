# Digital Certificate Repository — Single Window Upgrade

All application screens now open inside one main application window using Swing internal frames. Use the **← Back to Dashboard** / Back / close controls to return to the dashboard. The login screen remains separate and appears again after logout.

## Run on macOS
From this folder in Terminal:

```bash
rm -rf out && mkdir out
javac -cp "lib/mysql-connector-j-26.7.0.jar" -d out src/*.java
java -cp "out:lib/mysql-connector-j-26.7.0.jar" Main
```

Keep your locally working `src/DatabaseConnection.java` with your own MySQL password. Do not share that file publicly. If you replace this folder with this ZIP, copy your working DatabaseConnection.java into `src/` before running. The existing database is not dropped or re-created by this navigation change.
