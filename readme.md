This sample is to test how GWT's linkers behave around exceptions in the gwt iframe vs plain JS errors. Both
unhandled rejected promises and uncaught exceptions are tested in this sample.

To run this, test in both production mode and in super dev mode, but the specific case where we are seeing failures is
in super dev mode in Chrome. It is likely that the same failure would take place in Chrome if the nocache.js file was
loaded cross-origin, but the SDM CodeServer correctly sets CORS headers already, so this is a simpler way to test.

The top row of buttons tests rejected promises, and the bottom row tests uncaught errors. The first buttons emit errors
from buttons that are written in plain JS on the host page, while the later ones cause the errors to be emitted from GWT
code. The expected result is that each button causes some error which is noticed by GWT's event handlers, and further
that the exception object is the correct type when it is noticed by GWT.

### To run this sample:
 * check out the repo
 * start super dev mode with `mvn gwt:devmode`
 * load `http://localhost:8888/unhandledrejection` in your browser of choice

### Expected result: each button should log a message, and any time an exception is thrown, it should be logged as an
`IllegalStateException` rather than a `JsException`.

### Actual result: in chrome, in SDM, these are incorrect:
 * "click to reject (from gwt)" logs nothing
 * "click to throw IllegalStateException (from gwt)" incorrectly logs JsException
 * "click to throw in an event handler (from gwt)" incorrectly logs JsException

### Workaround:
Switch to branch 'workaround', which adds a modified `installScriptDirect.js` to the classpath when SDM
builds, which adds the `crossOrigin` attribute to each injected script tag.