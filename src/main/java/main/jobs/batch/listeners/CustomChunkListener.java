package main.jobs.batch.listeners;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

public class CustomChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext context) {
        System.err.println(Thread.currentThread() + " " + context);
    }

    @Override
    public void afterChunk(ChunkContext context) {
        System.err.println(Thread.currentThread() + " " + context);
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        System.err.println(context);
    }

}
