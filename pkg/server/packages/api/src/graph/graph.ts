import { GraphQLResolveInfo } from 'graphql';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type MakeEmpty<T extends { [key: string]: unknown }, K extends keyof T> = { [_ in K]?: never };
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
export type RequireFields<T, K extends keyof T> = Omit<T, K> & { [P in K]-?: NonNullable<T[P]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: { input: string; output: string; }
  String: { input: string; output: string; }
  Boolean: { input: boolean; output: boolean; }
  Int: { input: number; output: number; }
  Float: { input: number; output: number; }
};

export type AddZoneInput = {
  name: Scalars['String']['input'];
};

export type Location = {
  __typename?: 'Location';
  distance: Scalars['Float']['output'];
  id: Scalars['ID']['output'];
  latitude: Scalars['Float']['output'];
  longitude: Scalars['Float']['output'];
};

export type Mutation = {
  __typename?: 'Mutation';
  addZone: Zone;
};


export type MutationAddZoneArgs = {
  input: AddZoneInput;
};

export type Query = {
  __typename?: 'Query';
  ping?: Maybe<Scalars['String']['output']>;
  time?: Maybe<Scalars['String']['output']>;
  version?: Maybe<Scalars['String']['output']>;
  zone?: Maybe<Zone>;
  zones?: Maybe<Array<Maybe<Zone>>>;
};


export type QueryTimeArgs = {
  zone?: InputMaybe<Scalars['String']['input']>;
};


export type QueryZoneArgs = {
  id?: InputMaybe<Scalars['Int']['input']>;
  name?: InputMaybe<Scalars['String']['input']>;
};

export type SunPath = {
  __typename?: 'SunPath';
  culmination?: Maybe<Scalars['String']['output']>;
  rise?: Maybe<Scalars['String']['output']>;
  set?: Maybe<Scalars['String']['output']>;
};

export type Zone = {
  __typename?: 'Zone';
  altitude?: Maybe<Scalars['String']['output']>;
  azimuth?: Maybe<Scalars['String']['output']>;
  con?: Maybe<Scalars['String']['output']>;
  dec?: Maybe<Scalars['String']['output']>;
  dist?: Maybe<Scalars['String']['output']>;
  ecl_lat?: Maybe<Scalars['String']['output']>;
  ecl_long?: Maybe<Scalars['String']['output']>;
  elong?: Maybe<Scalars['String']['output']>;
  hx?: Maybe<Scalars['String']['output']>;
  hy?: Maybe<Scalars['String']['output']>;
  hz?: Maybe<Scalars['String']['output']>;
  id: Scalars['ID']['output'];
  mag?: Maybe<Scalars['String']['output']>;
  name: Scalars['String']['output'];
  ra?: Maybe<Scalars['String']['output']>;
};



export type ResolverTypeWrapper<T> = Promise<T> | T;


export type ResolverWithResolve<TResult, TParent, TContext, TArgs> = {
  resolve: ResolverFn<TResult, TParent, TContext, TArgs>;
};
export type Resolver<TResult, TParent = {}, TContext = {}, TArgs = {}> = ResolverFn<TResult, TParent, TContext, TArgs> | ResolverWithResolve<TResult, TParent, TContext, TArgs>;

export type ResolverFn<TResult, TParent, TContext, TArgs> = (
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => Promise<TResult> | TResult;

export type SubscriptionSubscribeFn<TResult, TParent, TContext, TArgs> = (
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => AsyncIterable<TResult> | Promise<AsyncIterable<TResult>>;

export type SubscriptionResolveFn<TResult, TParent, TContext, TArgs> = (
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => TResult | Promise<TResult>;

export interface SubscriptionSubscriberObject<TResult, TKey extends string, TParent, TContext, TArgs> {
  subscribe: SubscriptionSubscribeFn<{ [key in TKey]: TResult }, TParent, TContext, TArgs>;
  resolve?: SubscriptionResolveFn<TResult, { [key in TKey]: TResult }, TContext, TArgs>;
}

export interface SubscriptionResolverObject<TResult, TParent, TContext, TArgs> {
  subscribe: SubscriptionSubscribeFn<any, TParent, TContext, TArgs>;
  resolve: SubscriptionResolveFn<TResult, any, TContext, TArgs>;
}

export type SubscriptionObject<TResult, TKey extends string, TParent, TContext, TArgs> =
  | SubscriptionSubscriberObject<TResult, TKey, TParent, TContext, TArgs>
  | SubscriptionResolverObject<TResult, TParent, TContext, TArgs>;

export type SubscriptionResolver<TResult, TKey extends string, TParent = {}, TContext = {}, TArgs = {}> =
  | ((...args: any[]) => SubscriptionObject<TResult, TKey, TParent, TContext, TArgs>)
  | SubscriptionObject<TResult, TKey, TParent, TContext, TArgs>;

export type TypeResolveFn<TTypes, TParent = {}, TContext = {}> = (
  parent: TParent,
  context: TContext,
  info: GraphQLResolveInfo
) => Maybe<TTypes> | Promise<Maybe<TTypes>>;

export type IsTypeOfResolverFn<T = {}, TContext = {}> = (obj: T, context: TContext, info: GraphQLResolveInfo) => boolean | Promise<boolean>;

export type NextResolverFn<T> = () => Promise<T>;

export type DirectiveResolverFn<TResult = {}, TParent = {}, TContext = {}, TArgs = {}> = (
  next: NextResolverFn<TResult>,
  parent: TParent,
  args: TArgs,
  context: TContext,
  info: GraphQLResolveInfo
) => TResult | Promise<TResult>;



/** Mapping between all available schema types and the resolvers types */
export type ResolversTypes = {
  AddZoneInput: AddZoneInput;
  Boolean: ResolverTypeWrapper<Scalars['Boolean']['output']>;
  Float: ResolverTypeWrapper<Scalars['Float']['output']>;
  ID: ResolverTypeWrapper<Scalars['ID']['output']>;
  Int: ResolverTypeWrapper<Scalars['Int']['output']>;
  Location: ResolverTypeWrapper<Location>;
  Mutation: ResolverTypeWrapper<{}>;
  Query: ResolverTypeWrapper<{}>;
  String: ResolverTypeWrapper<Scalars['String']['output']>;
  SunPath: ResolverTypeWrapper<SunPath>;
  Zone: ResolverTypeWrapper<Zone>;
};

/** Mapping between all available schema types and the resolvers parents */
export type ResolversParentTypes = {
  AddZoneInput: AddZoneInput;
  Boolean: Scalars['Boolean']['output'];
  Float: Scalars['Float']['output'];
  ID: Scalars['ID']['output'];
  Int: Scalars['Int']['output'];
  Location: Location;
  Mutation: {};
  Query: {};
  String: Scalars['String']['output'];
  SunPath: SunPath;
  Zone: Zone;
};

export type LocationResolvers<ContextType = any, ParentType extends ResolversParentTypes['Location'] = ResolversParentTypes['Location']> = {
  distance?: Resolver<ResolversTypes['Float'], ParentType, ContextType>;
  id?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  latitude?: Resolver<ResolversTypes['Float'], ParentType, ContextType>;
  longitude?: Resolver<ResolversTypes['Float'], ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type MutationResolvers<ContextType = any, ParentType extends ResolversParentTypes['Mutation'] = ResolversParentTypes['Mutation']> = {
  addZone?: Resolver<ResolversTypes['Zone'], ParentType, ContextType, RequireFields<MutationAddZoneArgs, 'input'>>;
};

export type QueryResolvers<ContextType = any, ParentType extends ResolversParentTypes['Query'] = ResolversParentTypes['Query']> = {
  ping?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  time?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType, Partial<QueryTimeArgs>>;
  version?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  zone?: Resolver<Maybe<ResolversTypes['Zone']>, ParentType, ContextType, Partial<QueryZoneArgs>>;
  zones?: Resolver<Maybe<Array<Maybe<ResolversTypes['Zone']>>>, ParentType, ContextType>;
};

export type SunPathResolvers<ContextType = any, ParentType extends ResolversParentTypes['SunPath'] = ResolversParentTypes['SunPath']> = {
  culmination?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  rise?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  set?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type ZoneResolvers<ContextType = any, ParentType extends ResolversParentTypes['Zone'] = ResolversParentTypes['Zone']> = {
  altitude?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  azimuth?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  con?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  dec?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  dist?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  ecl_lat?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  ecl_long?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  elong?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  hx?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  hy?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  hz?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  id?: Resolver<ResolversTypes['ID'], ParentType, ContextType>;
  mag?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  name?: Resolver<ResolversTypes['String'], ParentType, ContextType>;
  ra?: Resolver<Maybe<ResolversTypes['String']>, ParentType, ContextType>;
  __isTypeOf?: IsTypeOfResolverFn<ParentType, ContextType>;
};

export type Resolvers<ContextType = any> = {
  Location?: LocationResolvers<ContextType>;
  Mutation?: MutationResolvers<ContextType>;
  Query?: QueryResolvers<ContextType>;
  SunPath?: SunPathResolvers<ContextType>;
  Zone?: ZoneResolvers<ContextType>;
};

