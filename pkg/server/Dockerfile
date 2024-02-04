FROM node:lts
WORKDIR /app/
COPY . .

RUN \
  yarn set version stable &&\
  yarn install &&\
  yarn build &&\
  yarn workspaces focus --production --all
