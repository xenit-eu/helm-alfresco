package eu.xenit.testing.k8s.kind;

import com.contentgrid.helm.HelmInstallCommand.InstallOption;
import com.contentgrid.junit.jupiter.helm.HelmChart;
import com.contentgrid.junit.jupiter.helm.HelmChartHandle;
import com.contentgrid.junit.jupiter.helm.HelmClient;
import com.contentgrid.junit.jupiter.k8s.KubernetesTestCluster;
import com.contentgrid.junit.jupiter.k8s.wait.KubernetesResourceWaiter;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KubernetesTestCluster
@HelmClient
class HelmAlfrescoTest {

    Logger logger = LoggerFactory.getLogger(HelmAlfrescoTest.class);

    static KubernetesClient kubernetesClient;

    @HelmChart(chart = "file:../xenit-alfresco")
    static HelmChartHandle alfrescoChart;

    @BeforeAll
    static void beforeAll() {
        var installed = alfrescoChart.install(
                InstallOption.values(Map.of(
                        "general.cni", "default",
                        "ingress.host", "test",
                        "ingress.protocol", "http",
                        "ingress.kubernetes.io/ingress.class", "")),
                InstallOption.values(Map.of(
                        "acs.replicas", 1,
                        "acs.resources.requests.memory", "2Gi",
                        "acs.resources.requests.cpu", "0",
                        "mq.resources.requests.memory", "1Gi",
                        "mq.resources.requests.cpu", "0",
                        "solr.enabled", false,
                        "transformServices.enabled", false,
                        "digitalWorkspace.enabled", false
                ))
        );

        new KubernetesResourceWaiter(kubernetesClient)
                .include(installed)
                .await(wait -> wait.atMost(5, TimeUnit.MINUTES));
    }

    @Test
    void testPodsReady() throws IOException {
        // if we get here the helm chart has started
        logger.info("PodsReady");
    }

}