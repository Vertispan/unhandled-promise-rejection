package com.vertispan.sample.unhandledrejection.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLButtonElement;
import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;

import static elemental2.dom.DomGlobal.*;

public class AppEntryPoint implements EntryPoint {

    // Helper to add a listener to gwt's own iframe, without a check if it is different from the host page
    @JsMethod(namespace = "<window>")
    private static native void addEventListener(String type, EventListener listener);

    @Override
    public void onModuleLoad() {
        HTMLButtonElement rejectBtn = (HTMLButtonElement) document.createElement("button");
        rejectBtn.textContent = "click to reject (from gwt)";
        rejectBtn.onclick = e -> {
            console.log("Rejecting a promise in an unhandled way from gwt");
            new Promise<>((resolve, reject) -> {
                setTimeout(reject::onInvoke, 100);
            }).then(happy -> {
                console.log("this will never happen");
                return Promise.resolve(happy);
            });
            return true;
        };

        HTMLButtonElement throwBtn = (HTMLButtonElement) document.createElement("button");
        throwBtn.textContent = "click to throw (from gwt)";
        throwBtn.onclick = e -> {
            console.log("Throwing in a promise in an unhandled way from gwt");
            new Promise<>((resolve, reject) -> {
                setTimeout(resolve::onInvoke, 100);
            }).then(v -> {
                throw new IllegalStateException("thrown exception");
            });
            return true;
        };
        document.body.append(rejectBtn, throwBtn);

        // ---

        addEventListener("unhandledrejection", e -> {
            console.log("gwt's own reject", e);
        });
        window.addEventListener("unhandledrejection", e -> {
            console.log("host page reject via gwt", e);
        });
//        EventListener lambda = e -> {
//            console.log("gwt noticed a unhandled reject", e);
//        };
//        addEventListener("unhandledrejection", lambda);
//        window.addEventListener("unhandledrejection", lambda);
    }
}
