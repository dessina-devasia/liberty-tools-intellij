/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.tools.intellij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Drives the STARTING/STOPPING in-progress spinner animation for the Liberty tool window tree.
 *
 * <p>Mirrors {@code SpinnerAnimator} from liberty-tools-eclipse. Cycles through
 * {@link #FRAME_COUNT} PNG frames at ~10 fps. When active modules exist, schedules a
 * repeating timer that advances the frame index and asks the tree to repaint; when
 * no animated modules remain the timer is stopped.</p>
 *
 * <p>Call {@link #setActive(boolean)} to start or stop the animation loop.
 * Call {@link #currentFrame()} from the tree renderer to get the icon for the current frame.
 * Call {@link #dispose()} when the tool window is torn down.</p>
 */
public class SpinnerAnimator {

    /** Total number of animation frames (matches the 12 SVG files). */
    public static final int FRAME_COUNT = 12;

    /** Delay between frames in milliseconds (~10 fps). */
    private static final int FRAME_DELAY_MS = 100;

    private final Tree tree;
    private final AtomicInteger frameIndex = new AtomicInteger(0);
    private volatile ScheduledFuture<?> scheduledFuture;
    private final ScheduledThreadPoolExecutor executor;

    /** Pre-loaded spinner frame icons indexed 0..FRAME_COUNT-1. */
    private final Icon[] frames = new Icon[FRAME_COUNT];

    /**
     * Creates a new SpinnerAnimator tied to the given tree.
     *
     * @param tree The Liberty tool window tree that will be repainted on each tick.
     */
    public SpinnerAnimator(Tree tree) {
        this.tree = tree;
        this.executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "liberty-spinner-animator");
            t.setDaemon(true);
            return t;
        });
        this.executor.setRemoveOnCancelPolicy(true);

        // Pre-load all frames once so the renderer never blocks on I/O.
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = LibertyPluginIcons.spinnerFrame(i + 1); // files are 1-based
        }
    }

    /**
     * Returns the icon for the current animation frame.
     * Falls back to the {@link LibertyPluginIcons#startingIcon()} if frames failed to load.
     */
    public Icon currentFrame() {
        Icon frame = frames[frameIndex.get() % FRAME_COUNT];
        return frame != null ? frame : LibertyPluginIcons.startingIcon();
    }

    /**
     * Starts the animation timer when {@code active} is {@code true},
     * or stops it when {@code false}.
     *
     * <p>Safe to call from any thread. Multiple calls with the same value are no-ops.</p>
     */
    public synchronized void setActive(boolean active) {
        if (active && scheduledFuture == null) {
            scheduledFuture = executor.scheduleAtFixedRate(this::tick, 0, FRAME_DELAY_MS, TimeUnit.MILLISECONDS);
        } else if (!active && scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

    /** Returns {@code true} when the animation loop is currently running. */
    public boolean isActive() {
        return scheduledFuture != null;
    }

    /**
     * Releases resources. Must be called when the tool window is disposed.
     */
    public void dispose() {
        setActive(false);
        executor.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private void tick() {
        frameIndex.updateAndGet(i -> (i + 1) % FRAME_COUNT);
        // Repaint the tree on the EDT.
        ApplicationManager.getApplication().invokeLater(() -> {
            if (tree != null && tree.isShowing()) {
                tree.repaint();
            }
        });
    }
}
