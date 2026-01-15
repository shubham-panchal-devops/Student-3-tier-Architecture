*** Build WAR file ***
Ensure Maven is installed:
mvn -version

Then run:
cd student-app
mvn clean package

WAR file is created at:
target/student-app.war

*** Verify WAR ***
ls -l target/*.war

Deploy WAR into Tomcat container
Your Helm Tomcat container expects:
/usr/local/tomcat/webapps/student-app.war

Your Dockerfile will COPY it:

FROM tomcat:9
COPY target/student-app.war /usr/local/tomcat/webapps/student-app.war

*** AWS ECR / Docker image steps (commands) ***

Replace <AWS_ACCOUNT_ID>, <region>, and <repo> accordingly.
Create ECR repositories

aws ecr create-repository --repository-name student-web --region <region>
aws ecr create-repository --repository-name student-app --region <region>


Authenticate Docker to ECR
aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com

Build & push web image (optional — configmap approach used in helm)
cd student3tier/web
docker build -t student-web:latest .
docker tag student-web:latest <AWS_ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/student-web:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/student-web:latest


Build & push app image
cd ../app
docker build -t student-app:latest .
docker tag student-app:latest <AWS_ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/student-app:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<region>.amazonaws.com/student-app:latest

Update values.yaml with the pushed image URIs (replace placeholders), and set secrets for DB passwords.

*** Helm install steps ***

Ensure kubectl context is set to cluster (AWS EKS, k3s on cloud that supports LoadBalancer, or minikube with minikube tunnel).
From project root:
helm upgrade --install student3tier ./student3tier -n student --create-namespace

Check resources:
kubectl get pods -n student
kubectl get svc -n student

Get external IP of web service:
kubectl get svc student-web -n student


Open the EXTERNAL-IP in your browser (port 80). If EXTERNAL-IP shows <pending>, your cluster may not have a cloud LB provisioner (minikube requires minikube tunnel or NodePort fallback). If using minikube, either change service type to NodePort and use nodeIP:nodePort, or run minikube tunnel.