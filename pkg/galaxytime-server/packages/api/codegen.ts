
import type { CodegenConfig } from '@graphql-codegen/cli';

const config: CodegenConfig = {
  overwrite: true,
  schema: "./src/graph/schema.graphql",
  generates: {
    "src/graph/graph.ts": {
      plugins: ["typescript", "typescript-resolvers"]
    },
    // "./graphql.schema.json": {
    //   plugins: ["introspection"]
    // }
  }
};

export default config;
