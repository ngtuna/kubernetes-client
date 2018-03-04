package io.fabric8.kubernetes.examples;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tuna on 3/4/18.
 */
public class CreateDeployment {
  private static final Logger logger = LoggerFactory.getLogger(CreateDeployment.class);

  public static void main(String[] args) throws InterruptedException {
    if (args.length < 2) {
      System.out.println("Usage: <deployment_name> <image_name> <image_port> <namespace>");
      return;
    }
    String deploymentName = args[0];
    String imageName = args[1];
    int imagePort = Integer.parseInt(args[2]);

    Config config = new ConfigBuilder().build();
    KubernetesClient client = new DefaultKubernetesClient(config);
    String namespace = client.getNamespace();
    if (args.length == 4) {
      namespace = args[3];
    }

    try {
      Deployment deployment = new DeploymentBuilder()
        .withNewMetadata()
        .withName(deploymentName)
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", deploymentName)
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(deploymentName)
        .withImage(imageName)
        .addNewPort()
        .withContainerPort(imagePort)
        .endPort()
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();

      deployment = client.extensions().deployments().inNamespace(namespace).create(deployment);
      log("Created deployment", deployment);

      Service service = new ServiceBuilder()
        .withNewMetadata()
        .withName(deploymentName)
        .endMetadata()
        .withNewSpec()
        .addNewPort()
        .withPort(imagePort)
        .withNewTargetPort(imagePort)
        .endPort()
        .addToSelector("app", deploymentName)
        .withType("ClusterIP")
        .endSpec()
        .build();

      service = client.services().inNamespace(namespace).create(service);
      log("Created service", service);

    }finally {
      client.close();
    }
  }

  private static void log(String action, Object obj) {
    logger.info("{}: {}", action, obj);
  }

  private static void log(String action) {
    logger.info(action);
  }
}
