package com.priya.devflow.config;

import com.priya.devflow.entity.PipelineRun;
import com.priya.devflow.entity.RunStatus;
import com.priya.devflow.entity.ScanResult;
import com.priya.devflow.entity.TrackedRepo;
import com.priya.devflow.repository.PipelineRunRepository;
import com.priya.devflow.repository.ScanResultRepository;
import com.priya.devflow.repository.TrackedRepoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Component
@Order(2)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String[] BRANCHES = {"main", "main", "main", "develop", "feature/auth", "fix/cache"};
    private static final String[] ACTORS = {"priya", "arjun", "meera", "rahul"};
    private static final String[] TOOLS = {"CodeQL", "Trivy", "Gitleaks", "OWASP Dependency-Check"};

    private final AppProperties props;
    private final TrackedRepoRepository repos;
    private final PipelineRunRepository runs;
    private final ScanResultRepository scans;

    public DemoDataSeeder(AppProperties props, TrackedRepoRepository repos, PipelineRunRepository runs,
                          ScanResultRepository scans) {
        this.props = props;
        this.repos = repos;
        this.runs = runs;
        this.scans = scans;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!props.demoData() || repos.count() > 0) {
            return;
        }
        Random random = new Random(42);
        String[][] seeds = {
                {"demo-org/payment-service", "Java"},
                {"demo-org/auth-gateway", "Java"},
                {"demo-org/web-storefront", "TypeScript"},
                {"demo-org/inventory-api", "Python"},
                {"demo-org/notification-worker", "Go"}
        };
        Instant now = Instant.now();
        for (String[] seed : seeds) {
            TrackedRepo repo = new TrackedRepo(seed[0]);
            repo.setLanguage(seed[1]);
            repo.setDefaultBranch("main");
            repo.setDescription("Sample repository (demo data)");
            repo.setLastSyncedAt(now);
            repos.save(repo);
            seedRuns(repo, random, now);
            seedScans(repo, random, now);
        }
        log.info("Seeded demo data for {} repositories", seeds.length);
    }

    private void seedRuns(TrackedRepo repo, Random random, Instant now) {
        long externalId = 1000L + random.nextInt(9000);
        for (int day = 13; day >= 0; day--) {
            int perDay = 1 + random.nextInt(4);
            for (int i = 0; i < perDay; i++) {
                externalId++;
                Instant started = now.minus(day, ChronoUnit.DAYS).minus(random.nextInt(600), ChronoUnit.MINUTES);
                int roll = random.nextInt(100);
                RunStatus status = roll < 74 ? RunStatus.SUCCESS : roll < 92 ? RunStatus.FAILED : RunStatus.CANCELLED;
                if (day == 0 && i == perDay - 1) {
                    status = random.nextBoolean() ? RunStatus.RUNNING : RunStatus.QUEUED;
                }
                PipelineRun run = new PipelineRun(repo, "CI Pipeline", status);
                run.setExternalId(externalId);
                run.setBranch(BRANCHES[random.nextInt(BRANCHES.length)]);
                run.setCommitSha(Long.toHexString(random.nextLong() & 0xfffffffffffL));
                run.setActor(ACTORS[random.nextInt(ACTORS.length)]);
                run.setTriggerEvent(random.nextInt(4) == 0 ? "pull_request" : "push");
                boolean finished = status != RunStatus.RUNNING && status != RunStatus.QUEUED;
                run.applyTiming(started, finished ? started.plusSeconds(90 + random.nextInt(420)) : null);
                runs.save(run);
            }
        }
    }

    private void seedScans(TrackedRepo repo, Random random, Instant now) {
        List<ScanResult> list = new java.util.ArrayList<>();
        for (String tool : TOOLS) {
            int crit = random.nextInt(10) < 2 ? 1 : 0;
            ScanResult scan = new ScanResult(repo, tool, crit, random.nextInt(4), random.nextInt(8), random.nextInt(12));
            scan.setScannedAt(now.minus(random.nextInt(48), ChronoUnit.HOURS));
            list.add(scan);
        }
        scans.saveAll(list);
    }
}
