# Serverless on Kubernetes with Kubeless
This repository has a basic step-by-step demo of running serverless functions on [Kubernetes](https://kubernetes.io/) using [Kubeless](https://github.com/kubeless/kubeless).

The demo includes deploying Kubeless to [Minikube](https://github.com/kubernetes/minikube) and deploying two example functions.

# Function runtimes
Kubeless supports multiple run times for its functions.
```bash
Supported Runtimes are:
python2.7, python3.4, python3.6, nodejs6, nodejs8, nodejs_distroless8, ruby2.4, php7.2, go1.10, dotnetcore2.0, java1.8, ballerina0.981.0, jvm1.8
```
See more details in [Kubeless runtimes documentation](https://kubeless.io/docs/runtimes/)


# Demo
Below is the step-by-step demo of setting up local Kubernetes with Minikube, deploying Kubeless and functions.

## Setup Kubernetes with Minikube
Startup a local Kubernetes 1 node cluster with Minikube (this example is for Mac OS).
```bash
# Install Minikube v0.28.2
$ curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.28.2/minikube-darwin-amd64 && \
        chmod +x minikube && \
        sudo mv minikube /usr/local/bin/

# Start up an RBAC enabled Minikube (v0.28.2)
$ minikube start --extra-config=apiserver.authorization-mode=RBAC
$ kubectl create clusterrolebinding add-on-cluster-admin \
        --clusterrole=cluster-admin \
        --serviceaccount=kube-system:default

# Get Minikube's IP (used later in the demo)
$ MINIKUBE_IP=$(minikube ip)

# Enable ingress controller
$ minikube addons enable ingress

# See the ingress controller pod
$ kubectl get pod -n kube-system -l app=nginx-ingress-controller
```

## Setup Kubeless
Deploy Kubeless
```bash
# Deploy the kubeless resources to the kubeless namespace
$ kubectl create ns kubeless
$ export RELEASE=$(curl -s https://api.github.com/repos/kubeless/kubeless/releases/latest | grep tag_name | cut -d '"' -f 4)
$ kubectl create -f https://github.com/kubeless/kubeless/releases/download/${RELEASE}/kubeless-${RELEASE}.yaml
```

You can see the Kubernetes objects created by Kubeless
```bash
# See kubeless pod
$ kubectl get pods -n kubeless

# See kubeless deployment
$ kubectl get deployment -n kubeless

# See kubeless CRDs
$ kubectl get customresourcedefinition
```

Install the `kubeless` CLI for easy interaction with the framework
```bash
# Installing kubeless CLI using execute:
$ export OS=$(uname -s| tr '[:upper:]' '[:lower:]')
$ curl -OL https://github.com/kubeless/kubeless/releases/download/${RELEASE}/kubeless_${OS}-amd64.zip && \
        unzip kubeless_${OS}-amd64.zip && \
        sudo mv bundles/kubeless_${OS}-amd64/kubeless /usr/local/bin/

# Check version
$ kubeless version

# See supported run-times
$ kubeless get-server-config
```

## Python sample function
A sample python function in [demo-python.py](functions/demo-python.py)

### Deploy function
```bash
$ kubeless function deploy demo-python \
        --runtime python2.7 \
        --handler demo-python.hello \
        --from-file functions/demo-python.py

# See functions
$ kubectl get functions

$ kubeless function ls
```

### Run function
```bash
# Using kubeless CLI
$ kubeless function call demo-python --data 'Hello python world!'

# Using http
$ kubectl proxy -p 8080 &
$ curl -L --data '{"Python": "Kube API"}' \
        --header "Content-Type:application/json" \
        localhost:8080/api/v1/namespaces/default/services/demo-python:http-function-port/proxy/

# Create a http trigger to get-python function:
$ kubeless trigger http create demo-python --function-name demo-python
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Python": "Ingress"}' \
        --header "Host: demo-python.${MINIKUBE_IP}.nip.io" \
        --header "Content-Type:application/json" \
        ${MINIKUBE_IP}

# Kubeless creates a default hostname in form of ..nip.io.
# Alternatively, you can provide a real hostname with --hostname flag or use a different --path like this:
$ kubeless trigger http create demo-python-hostname --function-name demo-python --path demo-python --hostname example.com
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Python": "Ingress", "With": "Host"}' \
        --header "Host: example.com" \
        --header "Content-Type:application/json" \
        ${MINIKUBE_IP}/demo-python
```

## Java sample function
A sample Java function in [demo-java.java](functions/demo-java.java)

### Deploy function
```bash
$ kubeless function deploy demo-java \
        --runtime java1.8 \
        --handler Demo.hello \
        --from-file functions/demo-java.java

# See functions
$ kubectl get functions

$ kubeless function ls
```

### Run function
```bash
# Using kubeless CLI
$ kubeless function call demo-java --data 'Hello java world!'

# Using http
$ kubectl proxy -p 8080 &
$ curl -L --data '{"Java": "Kube API"}' \
        --header "Content-Type:application/json" \
        localhost:8080/api/v1/namespaces/default/services/demo-java:http-function-port/proxy/

# Create a http trigger to get-python function:
$ kubeless trigger http create demo-java --function-name demo-java
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Java": "Ingress"}' \
        --header "Host: demo-java.${MINIKUBE_IP}.nip.io" \
        --header "Content-Type:application/json" \
        ${MINIKUBE_IP}

# Kubeless creates a default hostname in form of ..nip.io.
# Alternatively, you can provide a real hostname with --hostname flag or use a different --path like this:
$ kubeless trigger http create demo-java-hostname --function-name demo-java --path demo-java --hostname example.com
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Java": "Ingress", "With": "Host"}' \
        --header "Host: example.com" \
        --header "Content-Type:application/json" \
        ${MINIKUBE_IP}/demo-java
```


## Clean up
You can delete the functions and uninstall Kubeless:
```bash
$ kubeless function delete demo-python
$ kubeless function delete demo-java
```

Delete kubeless
```bash
$ kubectl delete -f https://github.com/kubeless/kubeless/releases/download/${RELEASE}/kubeless-${RELEASE}.yaml
```
