1.2
 * Use newest batch event framework. This fixes the issue where the component is run multiple times.

1.1
 * Sends mail on errors, see config in logback.xml
 * Retry on 409 errors from fedora with exponential backoff
 * logback.xml pattern synchronized to common format
 * Updated event name to match code so this component will not run FOREVER

1.0
Initial release
