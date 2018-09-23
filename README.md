# Serverless on Kubernetes with Kubeless
This repository has a basic demo of running serverless on Kubernetes using [Kubeless](https://github.com/kubeless/kubeless).

The demo includes deploying Kubeless to [Minikube](https://github.com/kubernetes/minikube) and deploying two example functions.

# Runtimes
Kubeless supports multiple run times for its functions.
```bash
Supported Runtimes are: python2.7, python3.4, python3.6, nodejs6, nodejs8, nodejs_distroless8, ruby2.4, php7.2, go1.10, dotnetcore2.0, java1.8, ballerina0.981.0, jvm1.8 
```
See details in [Kubeless runtimes documentation](https://kubeless.io/docs/runtimes/)  


# Demo
## Setup Kubernetes with Minikube
Startup a local Kubernetes 1 node cluster with Minikube.
```bash
# Install Minikube v0.28.2
$ curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.28.2/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/

# Start up an RBAC enabled Minikube (v0.28.2)
$ minikube start --extra-config=apiserver.authorization-mode=RBAC
$ kubectl create clusterrolebinding add-on-cluster-admin --clusterrole=cluster-admin --serviceaccount=kube-system:default

# Enable ingress controller
$ minikube addons enable ingress

# See the ingress controller pod
$ kubectl get pod -n kube-system -l app=nginx-ingress-controller
```

## Setup Kubeless
Deploy Kubeless
```bash
# Deploy the kubeless resources
$ export RELEASE=$(curl -s https://api.github.com/repos/kubeless/kubeless/releases/latest | grep tag_name | cut -d '"' -f 4)
$ kubectl create ns kubeless
$ kubectl create -f https://github.com/kubeless/kubeless/releases/download/$RELEASE/kubeless-$RELEASE.yaml


# See kubeless pod
$ kubectl get pods -n kubeless

# See kubeless deployment
$ kubectl get deployment -n kubeless

# See kubeless CRDs
$ kubectl get customresourcedefinition


# Installing kubeless CLI using execute:
$ export OS=$(uname -s| tr '[:upper:]' '[:lower:]')
$ curl -OL https://github.com/kubeless/kubeless/releases/download/$RELEASE/kubeless_$OS-amd64.zip && \
    unzip kubeless_$OS-amd64.zip && \
    sudo mv bundles/kubeless_$OS-amd64/kubeless /usr/local/bin/

# See supported run-times
$ kubeless get-server-config
INFO[0000] Current Server Config:                       
INFO[0000] Supported Runtimes are: python2.7, python3.4, python3.6, nodejs6, nodejs8, nodejs_distroless8, ruby2.4, php7.2, go1.10, dotnetcore2.0, java1.8, ballerina0.981.0, jvm1.8 
```

## Python sample function
A sample python function in [demo-python.py](functions/demo-python.py)

Deploy function
```bash
$ kubeless function deploy demo-python \
        --runtime python2.7 \
        --handler demo-python.hello \
        --from-file functions/demo-python.py

# See functions
$ kubectl get functions

$ kubeless function ls
```

Run function
```bash
# Using kubeless CLI
$ kubeless function call demo-python --data 'Hello python world!'

# Using http
$ kubectl proxy -p 8080 &
$ curl -L --data '{"Another": "python"}' \
    --header "Content-Type:application/json" \
    localhost:8080/api/v1/namespaces/default/services/demo-python:http-function-port/proxy/

# Create a http trigger to get-python function:
$ kubeless trigger http create demo-python --function-name demo-python
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Another": "python"}' \
    --header "Host: demo-python.192.168.99.100.nip.io" \
    --header "Content-Type:application/json" \
    192.168.99.100


# Kubeless creates a default hostname in form of ..nip.io.
# Alternatively, you can provide a real hostname with --hostname flag or use a different --path like this:
$ kubeless trigger http create demo-python-hostname --function-name demo-python --path demo-python --hostname example.com
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Another": "python"}' \
    --header "Host: example.com" \
    --header "Content-Type:application/json" \
    192.168.99.100/demo-python
```

## Java sample function
A sample nodejs function in [demo-java.java](functions/demo-java.java)

Deploy function
```bash
$ kubeless function deploy demo-java \
                --runtime java1.8 \
                --handler Demo.hello \
                --from-file functions/demo-java.java

# See functions
$ kubectl get functions

$ kubeless function ls
```

Run function
```bash
# Using kubeless CLI
$ kubeless function call demo-java --data 'Hello world!'

# Using http
$ kubectl proxy -p 8080 &
$ curl -L --data '{"Another": "Echo"}' \
    --header "Content-Type:application/json" \
    localhost:8080/api/v1/namespaces/default/services/demo-java:http-function-port/proxy/

# Create a http trigger to get-python function:
$ kubeless trigger http create demo-java --function-name demo-java
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Another": "Echo"}' \
    --header "Host: demo-java.192.168.99.100.nip.io" \
    --header "Content-Type:application/json" \
    192.168.99.100


# Kubeless creates a default hostname in form of ..nip.io.
# Alternatively, you can provide a real hostname with --hostname flag or use a different --path like this:
$ kubeless trigger http create demo-java-hostname --function-name demo-java --path demo-java --hostname example.com
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Another": "Echo"}' \
    --header "Host: example.com" \
    --header "Content-Type:application/json" \
    192.168.99.100/demo-java
```








## NodeJS sample function
A sample nodejs function in [demo-node.js](functions/demo-node.js)

Deploy function
```bash
$ kubeless function deploy demo-node \
        --runtime nodejs6 \
        --handler demo-node.hello \ 
        --from-file functions/demo-node.js

# See functions
$ kubectl get functions

$ kubeless function ls
```

Run function
```bash
# Using kubeless CLI
$ kubeless function call demo-node --data 'Hello world!'

# Using http
$ kubectl proxy -p 8080 &
$ curl -L --data '{"Another": "Echo"}' \
    --header "Content-Type:application/json" \
    localhost:8080/api/v1/namespaces/default/services/demo-node:http-function-port/proxy/

# Create a http trigger to get-python function:
$ kubeless trigger http create demo-node --function-name demo-node
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Another": "Echo"}' \
    --header "Host: demo-node.192.168.99.100.nip.io" \
    --header "Content-Type:application/json" \
    192.168.99.100


# Kubeless creates a default hostname in form of ..nip.io.
# Alternatively, you can provide a real hostname with --hostname flag or use a different --path like this:
$ kubeless trigger http create demo-node-hostname --function-name demo-node --path demo-node --hostname example.com
$ kubectl get ing

# Test the created http trigger with the following command:
$ curl --data '{"Another": "Echo"}' \
    --header "Host: example.com" \
    --header "Content-Type:application/json" \
    192.168.99.100/demo-node
```


## Clean up
You can delete the functions and uninstall Kubeless:
```bash
$ kubeless function delete demo-python
$ kubeless function delete demo-node
```

Delete kubeless
```bash
$ kubectl delete -f https://github.com/kubeless/kubeless/releases/download/$RELEASE/kubeless-$RELEASE.yaml
```
