package com.vertispan.sample.unhandledrejection.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLButtonElement;
import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;
import jsinterop.base.Js;

import static elemental2.dom.DomGlobal.*;

public class AppEntryPoint implements EntryPoint {

    // Helper to add a listener to gwt's own iframe window, without a check if it is different from the host page
    @JsMethod(namespace = "<window>")
    private static native void addEventListener(String type, EventListener listener);

    @Override
    public void onModuleLoad() {
        HTMLButtonElement rejectPromiseBtn = (HTMLButtonElement) document.createElement("button");
        rejectPromiseBtn.textContent = "click to reject (from gwt)";
        rejectPromiseBtn.onclick = e -> {
            console.log("Rejecting a promise in an unhandled way from gwt");
            new Promise<>((resolve, reject) -> {
                setTimeout(reject::onInvoke, 100);
            }).then(happy -> {
                console.log("this will never happen");
                return Promise.resolve(happy);
            });
            return true;
        };

        HTMLButtonElement throwPromiseBtn = (HTMLButtonElement) document.createElement("button");
        throwPromiseBtn.textContent = "click to throw (from gwt)";
        throwPromiseBtn.onclick = e -> {
            console.log("Throwing in a promise in an unhandled way from gwt");
            new Promise<>((resolve, reject) -> {
                setTimeout(resolve::onInvoke, 100);
            }).then(v -> {
                throw new IllegalStateException("thrown exception");
            });
            return true;
        };
        document.getElementById("promises").append(rejectPromiseBtn, throwPromiseBtn);

        // ---

        HTMLButtonElement throwBtn = (HTMLButtonElement) document.createElement("button");
        throwBtn.onclick = e -> {
            console.log("Throwing in an event handler in an unhandled way from gwt");

            throw new IllegalStateException("throw exception");
        };
        throwBtn.textContent = "click to throw in an event handler (from gwt)";

        document.getElementById("throws").append(throwBtn);

        // ---

        addEventListener("unhandledrejection", e -> {
            console.log("gwt's own reject", e);
        });
        window.addEventListener("unhandledrejection", e -> {
            console.log("host page reject via gwt", e);
        });
        window.addEventListener("error", e -> {
            Object error = Js.asPropertyMap(e).get("error");
            if (error == null) {
                error = "null";
            }
            console.log("gwt noticed a host page error " + fromObject(error).getClass());
        });
        addEventListener("error", e -> {
            Object error = Js.asPropertyMap(e).get("error");
            if (error == null) {
                error = "null";
            }
            console.log("gwt noticed its own iframe error " + fromObject(error).getClass());
        });
    }

    private static native Throwable fromObject(Object obj) /*-{
        //GWT2 impl using JSNI, see GWT.native.js for the j2cl impl
        return @java.lang.Throwable::of(*)(obj);
    }-*/;
}
