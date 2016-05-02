package com.netflix.vms.transformer.publish.workflow.job;

import com.netflix.vms.transformer.common.publish.workflow.PublicationJob;
import com.netflix.vms.transformer.publish.workflow.PublishWorkflowContext;
import com.netflix.vms.transformer.publish.workflow.job.framework.PublishWorkflowPublicationJob;

public abstract class PoisonStateMarkerJob extends PublishWorkflowPublicationJob {

    private final PublicationJob dependency;

    public PoisonStateMarkerJob(PublishWorkflowContext ctx, PublicationJob dependency, long cycleVersion) {
        super(ctx, "mark-poison-after-" + dependency.getJobName(), cycleVersion);
        this.dependency = dependency;
    }

    @Override
    public boolean isEligible() {
        if(dependency.hasJobFailed())
            return true;
        return dependency.hasJobFailed();
    }

    @Override
    protected boolean isFailedBasedOnDependencies() {
        return dependency.isComplete();
    }

}
